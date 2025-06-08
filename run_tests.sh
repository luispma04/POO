#!/bin/bash

# Enhanced Simulation Runner with optional visualization and cleanup
# Usage: ./run_simulations.sh [--visualize] [--clean]

# Default settings
VISUALIZE_FLAG=""
CLEAN_MODE=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --visualize)
            VISUALIZE_FLAG="--visualize"
            shift
            ;;
        --clean)
            CLEAN_MODE=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [--visualize] [--clean]"
            echo "  --visualize: Create grid visualization files (*Grid.txt) without simulation"
            echo "  --clean: Remove all *Results.txt and *Grid.txt files"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Clean mode - remove all Results and Grid files
if [[ "$CLEAN_MODE" == true ]]; then
    echo "Cleaning Results and Grid files..."
    if [[ -d "SIM" ]]; then
        rm -f SIM/*Results.txt SIM/*Grid.txt
        echo "Removed all *Results.txt and *Grid.txt files from SIM folder"
    else
        echo "SIM folder not found"
    fi
    exit 0
fi

# Function to create grid visualization from input file
create_grid_visualization() {
    local input_file="$1"
    local base_name=$(basename "$input_file" .txt)
    local output_file="SIM/${base_name}Grid.txt"
    
    echo "Creating grid visualization for $base_name..."
    
    # Read and parse the input file
    local line_num=0
    local width height xi yi xf yf nscz nobs
    local -a obstacles
    local -a special_zones
    local reading_zones=false
    local reading_obstacles=false
    local zones_left=0
    local obstacles_left=0
    
    while IFS= read -r line || [[ -n "$line" ]]; do
        line=$(echo "$line" | xargs) # trim whitespace
        [[ -z "$line" ]] && continue # skip empty lines
        
        ((line_num++))
        
        if [[ $line_num -eq 1 ]]; then
            # Parse first line: width height xi yi xf yf nscz nobs ...
            read -r width height xi yi xf yf nscz nobs rest <<< "$line"
            zones_left=$nscz
            obstacles_left=$nobs
        elif [[ "$line" =~ ^special\ cost\ zones:.*$ ]]; then
            reading_zones=true
            reading_obstacles=false
        elif [[ "$line" =~ ^obstacles:.*$ ]]; then
            reading_zones=false
            reading_obstacles=true
        elif [[ "$reading_zones" == true && $zones_left -gt 0 ]]; then
            # Read special cost zone: x1 y1 x2 y2 cost
            # Strip all whitespace including carriage returns
            line=$(echo "$line" | tr -d '\r\n' | xargs)
            special_zones+=("$line")
            ((zones_left--))
        elif [[ "$reading_obstacles" == true && $obstacles_left -gt 0 ]]; then
            # Read obstacle: x y
            # Strip all whitespace including carriage returns
            line=$(echo "$line" | tr -d '\r\n' | xargs)
            obstacles+=("$line")
            ((obstacles_left--))
        fi
    done < "$input_file"
    
    # Create the grid visualization
    {
        echo "Grid Visualization for $base_name"
        echo "=================================="
        echo "Grid size: ${width}x${height}"
        echo "Start: ($xi, $yi)"
        echo "End: ($xf, $yf)"
        echo ""
        
        # Create grid array
        declare -A grid
        
        # Initialize with empty spaces
        for ((y=1; y<=height; y++)); do
            for ((x=1; x<=width; x++)); do
                grid["$x,$y"]="."
            done
        done
        
        # Mark special cost zones (outline only)
        for zone in "${special_zones[@]}"; do
            if [[ -n "$zone" ]]; then
                # Clean the zone string and parse coordinates
                zone=$(echo "$zone" | tr -d '\r\n' | xargs)
                read -r x1 y1 x2 y2 cost <<< "$zone"
                
                # Validate coordinates are numeric
                if [[ "$x1" =~ ^[0-9]+$ ]] && [[ "$y1" =~ ^[0-9]+$ ]] && [[ "$x2" =~ ^[0-9]+$ ]] && [[ "$y2" =~ ^[0-9]+$ ]]; then
                    # Mark horizontal edges (top and bottom)
                    for ((x=x1; x<x2; x++)); do
                        # Bottom edge
                        if [[ $y1 -ge 1 && $y1 -le $height ]]; then
                            grid["$x,$y1"]="+"
                            grid["$((x+1)),$y1"]="+"
                        fi
                        # Top edge  
                        if [[ $y2 -ge 1 && $y2 -le $height ]]; then
                            grid["$x,$y2"]="+"
                            grid["$((x+1)),$y2"]="+"
                        fi
                    done
                    # Mark vertical edges (left and right)
                    for ((y=y1; y<y2; y++)); do
                        # Left edge
                        if [[ $x1 -ge 1 && $x1 -le $width ]]; then
                            grid["$x1,$y"]="+"
                            grid["$x1,$((y+1))"]="+"
                        fi
                        # Right edge
                        if [[ $x2 -ge 1 && $x2 -le $width ]]; then
                            grid["$x2,$y"]="+"
                            grid["$x2,$((y+1))"]="+"
                        fi
                    done
                fi
            fi
        done
        
        # Mark obstacles
        for obstacle in "${obstacles[@]}"; do
            if [[ -n "$obstacle" ]]; then
                # Clean the obstacle string and parse coordinates
                obstacle=$(echo "$obstacle" | tr -d '\r\n' | xargs)
                read -r x y <<< "$obstacle"
                
                # Validate coordinates are numeric
                if [[ "$x" =~ ^[0-9]+$ ]] && [[ "$y" =~ ^[0-9]+$ ]]; then
                    if [[ $x -ge 1 && $x -le $width && $y -ge 1 && $y -le $height ]]; then
                        grid["$x,$y"]="#"
                    fi
                fi
            fi
        done
        
        # Mark start and end points (but don't overwrite obstacles)
        if [[ "${grid["$xi,$yi"]}" != "#" ]]; then
            grid["$xi,$yi"]="S"
        fi
        
        if [[ "${grid["$xf,$yf"]}" != "#" ]]; then
            grid["$xf,$yf"]="E"
        fi
        
        # Print column headers
        printf "  "
        for ((x=1; x<=width; x++)); do
            printf "%d " "$x"
        done
        echo
        
        # Print grid (top to bottom, so y decreases)
        for ((y=height; y>=1; y--)); do
            printf "%d " "$y"
            for ((x=1; x<=width; x++)); do
                printf "%s " "${grid["$x,$y"]}"
            done
            echo
        done
        
        echo ""
        echo "Legend:"
        echo "S - Start point"
        echo "E - End point"
        echo "# - Obstacle"
        echo "+ - Special cost zone edge"
        echo ". - Empty space"
        
        # Show special cost zones info
        if [[ ${#special_zones[@]} -gt 0 ]]; then
            echo ""
            echo "Special Cost Zones:"
            for zone in "${special_zones[@]}"; do
                if [[ -n "$zone" ]]; then
                    zone=$(echo "$zone" | tr -d '\r\n' | xargs)
                    read -r x1 y1 x2 y2 cost <<< "$zone"
                    if [[ "$x1" =~ ^[0-9]+$ ]] && [[ "$y1" =~ ^[0-9]+$ ]] && [[ "$x2" =~ ^[0-9]+$ ]] && [[ "$y2" =~ ^[0-9]+$ ]]; then
                        echo "  ($x1,$y1) to ($x2,$y2) - Cost: $cost"
                    fi
                fi
            done
        fi
        
        # Show obstacles info
        if [[ ${#obstacles[@]} -gt 0 ]]; then
            echo ""
            echo "Obstacles:"
            for obstacle in "${obstacles[@]}"; do
                if [[ -n "$obstacle" ]]; then
                    obstacle=$(echo "$obstacle" | tr -d '\r\n' | xargs)
                    read -r x y <<< "$obstacle"
                    if [[ "$x" =~ ^[0-9]+$ ]] && [[ "$y" =~ ^[0-9]+$ ]]; then
                        echo "  ($x,$y)"
                    fi
                fi
            done
        fi
        
    } > "$output_file"
    
    echo "  Grid saved to: $output_file"
    echo
}

# Function to extract cost from final result line
extract_cost() {
    local output="$1"
    echo "$output" | grep "Best fit individual:" | sed 's/.*with cost \([0-9]*\)/\1/'
}

# Function to check if final point was reached
reached_final() {
    local output="$1"
    local final_line=$(echo "$output" | grep "Best fit individual:")
    if [[ $final_line == *"with cost"* ]]; then
        echo "true"
    else
        echo "false"
    fi
}

# Function to extract comfort (if no solution found)
extract_comfort() {
    local output="$1"
    # Look for the last comfort value in observations
    echo "$output" | grep "Comfort:" | tail -1 | sed 's/Comfort: \([0-9.]*\)/\1/' | head -1
}

# Function to compare two simulation results
# Returns 1 if first is better, 0 if second is better
is_better() {
    local output1="$1"
    local output2="$2"
    
    local reached1=$(reached_final "$output1")
    local reached2=$(reached_final "$output2")
    
    # If both reached final, compare costs (lower is better)
    if [[ "$reached1" == "true" && "$reached2" == "true" ]]; then
        local cost1=$(extract_cost "$output1")
        local cost2=$(extract_cost "$output2")
        if [[ $cost1 -lt $cost2 ]]; then
            echo 1
        else
            echo 0
        fi
    # If only first reached final, first is better
    elif [[ "$reached1" == "true" && "$reached2" == "false" ]]; then
        echo 1
    # If only second reached final, second is better
    elif [[ "$reached1" == "false" && "$reached2" == "true" ]]; then
        echo 0
    # If neither reached final, compare comfort (higher is better)
    else
        local comfort1=$(extract_comfort "$output1")
        local comfort2=$(extract_comfort "$output2")
        # Use awk for floating point comparison
        if [[ -n "$comfort1" && -n "$comfort2" ]]; then
            local result=$(awk "BEGIN {print ($comfort1 > $comfort2) ? 1 : 0}")
            echo $result
        else
            echo 0
        fi
    fi
}

# Function to run simulation multiple times and find best result
run_scenario() {
    local input_file="$1"
    local runs=20
    
    # Extract base name (e.g., "maze" from "SIM/maze.txt")
    local base_name=$(basename "$input_file" .txt)
    local output_file="SIM/${base_name}Results.txt"
    
    echo "Running $base_name..."
    
    local best_output=""
    local solutions_found=0
    
    for ((i=1; i<=runs; i++)); do
        echo -n "  Run $i/$runs..."
        
        # Run simulation without visualization for performance testing
        local current_output=$(java -jar "$JAR_PATH" -f "$input_file" 2>/dev/null)
        
        # Check if this is a valid output
        if [[ -n "$current_output" ]] && echo "$current_output" | grep -q "Best fit individual:"; then
            # Check if solution was found
            if [[ $(reached_final "$current_output") == "true" ]]; then
                ((solutions_found++))
                local cost=$(extract_cost "$current_output")
                echo " (SOLUTION: cost $cost)"
            else
                local comfort=$(extract_comfort "$current_output")
                echo " (comfort: ${comfort:-0.0})"
            fi
            
            # Compare with best so far
            if [[ -z "$best_output" ]] || [[ $(is_better "$current_output" "$best_output") -eq 1 ]]; then
                best_output="$current_output"
            fi
        else
            echo " (failed)"
        fi
    done
    
    # Save best result
    if [[ -n "$best_output" ]]; then
        echo "$best_output" > "$output_file"
        echo "  Solutions found: $solutions_found/$runs"
        if [[ $(reached_final "$best_output") == "true" ]]; then
            local best_cost=$(extract_cost "$best_output")
            echo "  Best cost: $best_cost"
        else
            local best_comfort=$(extract_comfort "$best_output")
            echo "  Best comfort: ${best_comfort:-0.0}"
        fi
        echo "  Saved to: $output_file"
    else
        echo "  No valid results obtained"
    fi
    echo
}

# Main execution
if [[ -n "$VISUALIZE_FLAG" ]]; then
    echo "=== GRID VISUALIZATION GENERATOR (No Simulation) ==="
else
    echo "=== PATHFINDER SIMULATION RUNNER ==="
fi
echo

# For visualization mode, we don't need the JAR file
if [[ -z "$VISUALIZE_FLAG" ]]; then
    # Check if project.jar exists (try both locations)
    JAR_PATH=""
    if [[ -f "project.jar" ]]; then
        JAR_PATH="project.jar"
    elif [[ -f "out/project.jar" ]]; then
        JAR_PATH="out/project.jar"
    else
        echo "Error: project.jar not found (checked current directory and out/ folder)" >&2
        exit 1
    fi
fi

# Check if SIM folder exists
if [[ ! -d "SIM" ]]; then
    echo "Error: SIM folder not found" >&2
    exit 1
fi

# Find all .txt files in SIM folder and run them
txt_files=(SIM/*.txt)
if [[ ! -e "${txt_files[0]}" ]]; then
    echo "No .txt files found in SIM folder"
    exit 1
fi

for input_file in "${txt_files[@]}"; do
    if [[ -f "$input_file" ]] && [[ ! "$input_file" == *"Results.txt" ]] && [[ ! "$input_file" == *"Grid.txt" ]]; then
        if [[ -n "$VISUALIZE_FLAG" ]]; then
            create_grid_visualization "$input_file"
        else
            run_scenario "$input_file"
        fi
    fi
done

if [[ -n "$VISUALIZE_FLAG" ]]; then
    echo "=== GRID VISUALIZATION COMPLETED ==="
else
    echo "=== SIMULATION COMPLETED ==="
fi