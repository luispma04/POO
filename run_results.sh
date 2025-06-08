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
            echo "  --visualize: Include grid visualization in output"
            echo "  --clean: Remove all *Results.txt files"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Clean mode - remove all Results files
if [[ "$CLEAN_MODE" == true ]]; then
    echo "Cleaning Results files..."
    if [[ -d "SIM" ]]; then
        rm -f SIM/*Results.txt
        echo "Removed all *Results.txt files from SIM folder"
    else
        echo "SIM folder not found"
    fi
    exit 0
fi

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
        
        # Run simulation with or without visualization
        local current_output
        if [[ -n "$VISUALIZE_FLAG" ]]; then
            current_output=$(java -jar "$JAR_PATH" -f "$input_file" $VISUALIZE_FLAG 2>/dev/null)
            # Strip ANSI color codes for clean text output
            current_output=$(echo "$current_output" | sed 's/\x1B\[[0-9;]*[JKmsu]//g')
        else
            current_output=$(java -jar "$JAR_PATH" -f "$input_file" 2>/dev/null)
        fi
        
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
echo "=== PATHFINDER SIMULATION RUNNER ==="
echo "Visualization: ${VISUALIZE_FLAG:-disabled}"
echo

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
    if [[ -f "$input_file" ]] && [[ ! "$input_file" == *"Results.txt" ]]; then
        run_scenario "$input_file"
    fi
done

echo "=== COMPLETED ==="