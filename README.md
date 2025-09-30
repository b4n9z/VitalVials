# VitalVials Plugin

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-green.svg)
![Bukkit](https://img.shields.io/badge/Bukkit-1.21.x_Spigot--API-red.svg)
![Spigot](https://img.shields.io/badge/Spigot-1.21.x_Spigot--API-orange.svg)
![Paper](https://img.shields.io/badge/Paper-1.21.x_Spigot--API-blue.svg)

VitalVials is a unique Minecraft plugin that allows players to purchase and manage in-game effects using their health. With a fully customizable configuration, server owners can create a balanced between risk and reward, where players must carefully consider the cost of powerful effects against their own vitality.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
- [Effect Customization](#effect-customization)
- [Scoreboard](#scoreboard)
- [Support](#support)
- [License](#license)

## Features

- **Health-Based Economy**: Players trade their max health / health points for powerful effects
- **Fully Configurable**: Customize every aspect of effects, costs, and mechanics
- **Dynamic Scoreboard**: Real-time tracking of active effects and remaining duration
- **Player-Specific Settings**: Each player can customize their scoreboard
- **Cooldown System**: Prevent effect spamming with configurable cooldowns
- **Refund System**: Option to refund effects (configurable per effect)

## Installation

1. Download the latest version of VitalVials from the [releases page](https://github.com/b4n9z/VitalVials/releases)
2. Place the `VitalVials.jar` file in your server's `plugins/` directory
3. Restart your server
4. Configure the plugin to your liking in `plugins/VitalVials/config.yml` and `plugins/VitalVials/effects.yml` and `plugins/VitalVials/activation.yml`
5. Use `/vv reload` to apply changes without restarting

## Commands

### Main Command
- `/vv` or `/vitalvials` - Main command for VitalVials

### Subcommands
- `/vv reload` - Reload the plugin configuration
- `/vv shop` - Opens the effect shop
- `/vv buyEffect <effect>` - Buy an effect directly
- `/vv viewStatus` - View your current active effects
- `/vv scoreboard <on/off/editShort>` - Toggle or customize your scoreboard
- `/vv refundEffect <effect>` - Refund an effect
- `/vv removeData <player/allPlayer>` - Remove player data

## Permissions

| Permission        | Description                        | Default |
|-------------------|------------------------------------|---------|
| `vv.admin`        | Full access to all commands        | op      |
| `vv.reload`       | Allows reloading the plugin        | op      |
| `vv.shop`         | Access to the effect shop          | op      |
| `vv.buyEffect`    | Allows buying effects              | op      |
| `vv.viewstatus`   | View own effect status             | op      |
| `vv.scoreboard`   | Access to scoreboard customization | op      |
| `vv.refundEffect` | Refund purchased effects           | op      |
| `vv.removedata`   | Remove player data                 | op      |

## Configuration

VitalVials is highly configurable through the `config.yml`, `effects.yml` and `activation.yml` file. Here are the main sections:

### Main Configuration
<details>
<summary>Click to expand</summary>
The configuration file (config.yml) allows you to customize several aspects of the plugin:

```yaml
effects: effects.yml # Path to the effects.yml file that contains the effects list
activation: activation.yml # Activation settings
maxHP: # max HP
  enabled: true # Enable / Disable max HP, when enabled players can't get more HP when trying to Refund Effect, so player can't get more HP that you set
  value: 20 # Max HP (Health Points)
minHP: 2 # Min HP (Health Points) to allow player not buying effect when HP is same or less than this
scoreboard: # scoreboard settings
  defaultShowScoreboard: true # Show scoreboard on right side by default (per player can disable it on their own using command if you give them access)
  periodUpdate: 10 # period to update scoreboard (in seconds)
permissionsAllPlayer:
  reload: false # Allow reload command for all players
  shop: true # Allow shop command for all players
  buyEffect: true # Allow buyEffect command for all players
  viewStatus: true # Allow viewStatus command for all players
  scoreboard: true # Allow scoreboard command for all players
  refundEffect: true # Allow refundEffect command for all players
  removeData: false # Allow removeData command for all players
```
</details>

### Effect Configuration
Each effect can be configured with the following options:
- `enabled`: Enable/disable the effect
- `name`: The name of the effect in shop
- `effect`: The name of the effect to give (default), don't change if you don't know what it is
- `lore`: The lore of the effect in shop
- `maxLevel`: The max level/amplifier of the effect
- `particles`: Whether particles will be shown when getting the effect
- `saveEffectData`: Whether the effect data will be saved to the player data
- `priceType`: The type of price when buying the effect (MAX_HEALTH to buy using MAX HEALTH player or HEALTH to buy using HEALTH player)
- `refundPercentage`: The percentage of the HP refundable (calculate from level 1 effect) after buying the effect (-1 to disable refund)
- `autoActivate`: Whether the effect will be auto activated after buying, if false you need set it in activation config
Array section (`[<level 1>, <level 2>, etc]`):
- `durationPerUpgrade`: Duration in seconds per upgrade, -1 for infinite (in seconds)
- `cooldownPerUpgrade`: Cooldown per upgrade (in seconds)
- `costPerUpgrade`: Health cost to activate per upgrade (Health Points)

### Activation Configuration
Each item can be configured with the following options:
- `rightClick`: Effects to activate to your own on right click with the item you configure
- `leftClick`: Effects to activate to your own on left click with the item you configure
- `YouAreHittingEnemy`: Effects to activate to your enemy when you are hitting an enemy with the item you configure
- `enemyHitYou`: Effects to activate to your enemy when an enemy hits you with the item you configure

## Effect Customization

Effects are defined in the `effects.yml` of the plugin folder. Example:
<details>
<summary>Click to expand</summary>
The configuration file (effects.yml) allows you to customize all effects in the plugin:

```yaml
speed: #key of the effect section, you can change it to anything you want, you can also use same effect to another key with different activation config
  enabled: true # enable/disable the effect
  name: "§bSpeed" # The name of the effect
  effect: "SPEED" # Name of the effect to give (default), don't change if you don't know what it is
  lore: "Get more speed" # lore of the effect (Informational only)
  maxLevel: 2 # max amplifier/level when upgrading the effect
  particles: false # enable/disable particles when getting effect
  saveEffectData: true # save effect data to the player data (If “false”, the effect can only be activated when “autoActivate” is true and can only be used once and player can't upgrade it)
  priceType: MAX_HEALTH # the type of price when buying the effect (MAX_HEALTH to buy using MAX HEALTH player or HEALTH to buy using HEALTH player)
  refundPercentage: 50 # the percentage of the HP refundable (calculate from level 1 effect) after buying the effect (-1 to disable refund)
  autoActivate: true # auto activate the effect, if true effect will be auto activated after buying
  # if false you need set it in activation config
  #=====[array section]=====
  # [<first upgrade level>, <second upgrade level>, ...]
  durationPerUpgrade: [-1, -1] # duration of the effect per upgrade, -1 for infinite (in seconds)
  cooldownPerUpgrade: [0, 0] # cooldown when using the effect per upgrade (in seconds)
  costPerUpgrade: [10, 18] # cost when buying the effect per upgrade (Health Points)
```
</details>

## Activation Customization

You can customize activation of effects in the plugin by editing the `activation.yml` file.
<details>
<summary>Click to expand</summary>
The configuration file (activation.yml) allows you to customize activation of effects in the plugin:

```yaml
ALL_ITEMS: # Effects to activate to your own with all items or no item
  rightClick: [] # Effects to activate to your own on right click with all items or no item
  leftClick: [] # Effects to activate to your own on left click with all items or no item
  YouAreHittingEnemy: [] # Effects to activate when you are hitting an enemy with all items or no item
  enemyHitYou: [poison, wither, darkness] # Effects to activate when an enemy hits you with all items or no item
SWORD: # Item name (all type of swords)
  rightClick: [resistance] # Effects to activate to your own on right click with the item
  leftClick: [] # Effects to activate to your own on left click with the item
  YouAreHittingEnemy: [nausea, weakness, weaving, oozing, infested] # Effects to activate when you are hitting an enemy with the item
  enemyHitYou: [slowness, mining_fatigue] # Effects to activate when an enemy hits you with the item
```
</details>

## Scoreboard

The scoreboard can be toggled with `/vv scoreboard` and customized with these options:
- Toggle visibility
- Change display order of effects

## Support

For support, please [open an issue](https://github.com/b4n9z/VitalVials/issues) on GitHub.

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/b4n9z/VitalsVials/blob/main/LICENSE) file for details.

---

Crafted with ❤️ by b4n9z
