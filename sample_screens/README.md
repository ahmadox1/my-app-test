# Sample Screens Directory

This directory contains sample screenshots for testing the game state extraction capabilities of Smart Coach.

## Structure

- `clash_royale/` - Screenshots from Clash Royale gameplay
- `clash_of_clans/` - Screenshots from Clash of Clans gameplay  
- `test_results/` - Analysis results and extracted data

## Usage

The vision analysis system will process these images to test and improve:

1. **Elixir Detection** - Extracting current elixir values for both players
2. **Card Recognition** - Identifying cards in hand and their elixir costs
3. **Unit Detection** - Finding units on the battlefield and their positions
4. **Event Recognition** - Detecting battle events and state changes

## Testing Commands

```bash
# Run vision analysis tests
./gradlew :core-vision:test

# Test strategy engine with sample data
./gradlew :core-strategy:test

# Full integration test
./gradlew test
```

## Adding New Samples

When adding new sample screenshots:

1. Use descriptive filenames (e.g. `battle_mid_game_elixir_7_3.png`)
2. Include ground truth data in corresponding `.json` files
3. Add annotations for expected extraction results

## Ground Truth Format

```json
{
  "filename": "battle_screenshot.png",
  "expected_results": {
    "my_elixir": 7,
    "opp_elixir": 3,
    "cards_in_hand": [
      {"name": "Giant", "cost": 5},
      {"name": "Wizard", "cost": 5},
      {"name": "Arrows", "cost": 3},
      {"name": "Knight", "cost": 3}
    ],
    "units_on_field": [
      {"name": "Enemy Tower", "position": [0.5, 0.1], "health": 100, "is_ally": false}
    ]
  }
}
```