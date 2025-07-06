# Table of Contents
- [Tablet of Contents](#table-of-contents)
- [Getting started](#getting-started)
- [Configurations](#configurations)
	+ [`config.yml`](#global-configyml)
	+ [`eggs.yml`](#eggs-eggsyml)
	+ [`gui.yml`](#pet-menu-guiyml)
	+ [`items.yml`](#default-items-itemsyml)
- [Adding a pet](#adding-a-pet)
- [Expressions](#expressions)
- [Advanced](#advanced)
- [References](#references)
	+ [Global namespaces](#global-namespaces)
	+ [Pet components](#pet-components)
		+ [`general`](#pet-component-general)
		+ [`display`](#pet-component-display)
		+ [`leveling`](#pet-component-leveling)
		+ [`stamina`](#pet-component-stamina)
		+ [`trigger`](#pet-component-trigger)
	+ [Trigger types](#trigger-types)
		+ [`walk`](#trigger-type-walk)
		+ [`release`](#trigger-type-release)

# Getting started

[⬅️ Back to Table of Contents](#table-of-contents)

Below are the prerequisites for PassivePet:

- Paper 1.21.6 or higher
	+ **Must use Paper as server software!** PassivePet uses new Paper experimental features, like item components,
	Adventure's MiniMessage or modern commands. Therefore, the plugin will not work on non-Paper forks, like Spigot or
	CraftBukkit.
- Java 21 or higher

## Modern commands

PassivePet uses modern commands system for its commands. Because of this, command like `/execute` will works just fine
with PassivePet. For example, to force player to open pet menu:

```
/execute as nahkd123 run pets
```

This command can be stored in command blocks or `.mcfunctions`.

# Configurations

[⬅️ Back to Table of Contents](#table-of-contents)

## Global (`config.yml`)

```yaml
globalMaxSlots: 1000
slotPermission: petstorage.slot.%s
```

- `globalMaxSlots`: The maximum number of slots all players in server can have. If a certain slot is blocked because
player doesn't have permission, the slot will be marked as "locked".
- `slotPermission`: The permission to check whether player have unlocked the pet slot. `%s` will be replaced with slot
number when checking the player's pet storage capacity.

## Eggs (`eggs.yml`)

```yaml
<egg id>:
	name: "<green>Egg name in MiniMessage format"
	duration: 1w2d3h4m5s
	rarity: 1
	pets:
	-	<pet id>
	-	...

<another egg id...>:
	...
```

- `duration`: The egg hatching duration, following the `WwDdHhMmSs` format:
	+ `w`: Weeks (1 week = 7 days)
	+ `d`: Days (1 day = 24 hours)
	+ `h`: Hours (1 hour = 60 minutes)
	+ `m`: Minutes (1 minute = 60 seconds)
	+ `s`: Seconds
- `rarity`: The rarity value of the pet that will be hatched from the egg. The value will be assigned directly to pet's
`hatching` component and can be retrieved by using `hatching.rarity` expression.

## Pet menu (`gui.yml`)

This configuration file only applies to `/pets` menu.

```yaml
title: Title of the menu
size: 54

border:
	type: MATERIAL_ID
	name: ...
	lore:
	-	...
	-	...
	tooltip: false                       # Hide tooltip when hovering on item

emptySlot: ...                           # Unused slot by player
lockedSlot: ...                          # Locked slot (no permission)
voidSlot: ...                            # Globally locked slot
egg: ...                                 # Hatching egg
pet: ...                                 # Inactive pet
activePet: ...                           # Active (currently used) pet
nextPage: ...                            # Next page button
prevPage: ...                            # Previous page button
```

## Default items (`items.yml`)

> [!NOTE]
> This configuration may only be used if there are no item management plugins installed (such as MMOItems or ItemAdder).
> If you have such item management plugins installed, you may consider using the PassivePet-specific options inside
> those plugins when creating new items.

# Adding a pet

[⬅️ Back to Table of Contents](#table-of-contents)

Pets can be added by creating a new YAML file in `PassivePet/pets/` folder. An empty YAML file is a valid (and also the
minimal) pet config file.

To add functionality for new pet, add pet components into the YAML file, which are the top-level YAML elements. Below is
an example of a pet with `general` and `display` component:

```yaml
general:
	name: "<aqua>TV Man"
	texture: https://textures.minecraft.net/texture/dd871e28db04d3711792e0fa549e997f846ac950412ba091a606f324459d38d3
	description:
	-	"<!i>Description in MiniMessage format"
	- 	"There can be more than 1 line"

display:
	texture: https://textures.minecraft.net/texture/dd871e28db04d3711792e0fa549e997f846ac950412ba091a606f324459d38d3
```

With the content above, a new pet will have the name "TV Man" with TV skin as head texture, which will be displayed in
pet menu. It's also displayed as floating head in the world when summoned by player. All pet components can be found in
[Pet components reference](#pet-components).

# Expressions

[⬅️ Back to Table of Contents](#table-of-contents)

In some places, you are allowed to specify value as expression, which is basically the formula that will be calculated
by plugin. For example, `42 * 1337` is an expression, where the number $42$ is multiplied by $1337$.

Expression can have variables. The list of variables depend on where the expression is being used. For example, if you
are using the expression to calculate the maximum EXP, you can get the level with `leveling.level`, and if you are using
expression to trigger something, you can give item to player with `player.giveItem(items.STONE)`. To see the list of
global variables, see [Global namespaces reference](#global-namespaces) and [Pet components reference](#pet-components).

In expression, the following symbols can be used to create a formula:

- `+` - Addition: Adding 2 values together;
- `-` - Subtraction: Subtracting the left value with the right one;
- `*` - Multiplication: Multiply the left value by right value;
- `/` - Division: Divide the left value by right value.

Comparision can be done by using any of the following symbols (which will return `1` if the comparision is true, or `0`
otherwise):

- `==`: 2 values are equals;
- `!=`: 2 values are NOT equals;
- `>`: Left value is greater than right;
- `>=`: Left value is greater than or equals to right;
- `<`: Left value is smaller than right;
- `<=`: Left value is smaller than or equals to right.

There are also bitwise operators, but these are more advanced for average server admins.

Expressions are evaluated using the following order of operation, sorted from first to last:

- Anything in parenthesis
- Bitwise: `<<`, `>>`, `&`, `|`, `^`
- Math: `*`, `/`
- Math: `+`, `-`
- Comparision: `==`, `!=`, `>`, `>=`, `<`, `<=`

Expression can be used to produce text. The text is quoted between single-quotation (`'`) or double-quotation (`"`)
marks, like `"hello world"` or `'hello world'`.

Expression can also be used to call a function. To call a function, put all parameters in parenthesis `()`, each
separated by a comma (`,`). After executation of a function, it may return a value, depending on which function you are
calling. Each parameter of a function is a smaller expression, which means you can nest function inside function, like
this:

```
math.sin(math.cos(x) * math.pi)
```

With the expression above, the final value will be calculated as $\sin(\cos(x) * \pi)$.

To get a property of specific value, use the dot (`.`) character, followed by the property name. Examples:

- `player.giveItem(items.STONE)`: Get `giveItem` of `player` variable, then get `STONE` of `items` and lastly, call
`giveItem()` with value obtained from `items.STONE`.
- `"hello world".uppercase`: Get `uppercase` of `"hello world"`, which returns `"HELLO WORLD"`.

# Advanced

[⬅️ Back to Table of Contents](#table-of-contents)

This section is for more advanced users.

## Advanced expressions

The entire backbone of expressions is powered by nahkd's [TinyExpr](https://github.com/nahkd123/tinyexpr) library. This
library basically bring the expression language that looks like Java to YAML configuration files. If you are using the
expression in PassivePet, you have been writing some of Java code!

Ternary operator consists of 3 smaller expressions: the first one for picking value based on whether the expression
returns non-zero or zero value, and left + right expressions that will be evaluated depending on the first one.

```
condition ? first : second
```

In this case, if `condition` returns a value that is not `0`, `first` will be evaluated and returned, otherwise,
`second` will be evaluated and returned.

If the value can be indexed, an indexing expression can be encapsulated in square brackets (`[]`), like this:

```
value[42 * 1337]
```

In this case, it will try to get the `42 * 1337 + 1`-th value inside `value`.

# References

[⬅️ Back to Table of Contents](#table-of-contents)

## Global namespaces

- `math`: Math helper functions: `math.sin()`, `math.cos()`, `math.tan()`, etc...
	+ All angles must be in radians.
	+ `math.pi`: The $\pi$ constant (3.1415926535898)
	+ `math.pow(x, exp)`: $x^{exp}$
	+ `math.sqrt(x)`: $\sqrt{x}$
	+ `math.log2(x)`: $\log_2(x)$
	+ `math.log10(x)`: $\log_{10}(x)$
	+ `math.min(a, b)`: Pick the smallest value between $a$ and $b$
	+ `math.max(a, b)`: Pick the largest value between $a$ and $b$
	+ `math.sin(angle)`
	+ `math.cos(angle)`
	+ `math.tan(angle)`
	+ `math.asin(value)`: Inverse sine
	+ `math.acos(value)`: Inverse cosine
	+ `math.atan(value)`: Inverse tan
	+ `math.atan2(y, x)`: Inverse tan with value = y / x
	+ `math.clamp(x, min, max)`: Clamp value so that $x \in [\text{min};\text{max}]$
- `items`: Vanilla item factory
	+ `items.<material-id>`: Create item with amount of 1 from material ID
	+ `.withAmount(amount)`: Set the amount of item
	+ `.withName(name)`: Set the name of item, using [MiniMessage][minimessage] text
	+ `.withLore(line1, line2, ...)`: Set the lore of item, using [MiniMessage][minimessage] text
	+ For example: `items.STONE.withName("<red>Red Stone").withLore("<!i><green>Green lore!")`
- `mmoitems`: (MMOItems must be installed): MMOItems factory
	+ `mmoitems(type, id)`: Create item with given ID from given type
	+ For example: `mmoitems("MISC", "COOL_ITEM")`
- `player`: (Only in trigger script) Interact with player
	+ `player.giveItem(item)`: Give `item` to player

## Pet components

### Pet component: `general`

Provide general information for the pet

```yaml
general:
	name: "Name of the pet in MiniMessage format"
	texture: "https://textures.minecraft.net/texture/..." # Only display in menu
	description:
	- "Description in MiniMessage format"
	- "..."
```

- Use `{{ general }}` in `gui.yml` to insert the description of the pet.

### Pet component: `display`

Provide 3D world display information for the pet

```yaml
display:
	texture: "https://textures.minecraft.net/texture/..." # Only display in world
```

### Pet component: `leveling`

Provide leveling information, as well as storing leveling data of the pet

```yaml
leveling:
	maxLevel: "expression..."
	maxExp: "expression..."
	maxEvolution: "expression..."
```

- `leveling.exp`: Pet's current EXP
- `leveling.level`: Pet's current level
- `leveling.evolution`: Pet's current evolution
- `leveling.maxExp`: Pet's maximum EXP for current level (calculated)
- `leveling.maxLevel`: Pet's maximum level (calculated)
- `leveling.maxEvolution`: Pet's maximum evolution (calculated)
- `leveling.setExp(exp)`: Set pet's current EXP
- `leveling.addExp(amount)`: Add EXP to pet and optionally leveling it up
- `leveling.setLevel(level)`: Set pet's current level
- `leveling.setEvolution(evolution)`: Set pet's current evolution
- `leveling.evolve()`: Increase pet's evolution by 1
- Use `{{ leveling }}` in `gui.yml` to insert the leveling information.

### Pet component: `stamina`

Provide stamina information, as well as storing pet's stamina

```yaml
stamina:
	maxStamina: "expression..."
```

- `stamina.value`: Pet's current stamina value
- `stamina.max`: Pet's maximum stamina
- `stamina.set(value)`: Set pet's current stamina
- `stamina.add(amount)`: Add stamina to pet
- `stamina.take(amount)`: Take stamina from pet
- `stamina.tryTaking(amount)`: Attempt to take some amount of stamina from pet, return `1` if successful, otherwise `0`.
- Use `{{ stamina }}` in `gui.yml` to insert stamina information.

### Pet component: `trigger`

Provide trigger scripts that perform action when something triggered

```yaml
trigger:
-	type: trigger type
	script: trigger script...
```

- `trigger.<variable-name>`: Depending on trigger type, the `trigger.<variable-name>` returns different value. See
[Trigger types](#trigger-types) section for more info.

The `script` can be a single expression, a list of smaller scripts, or conditional (see below)

```yaml
# A single expression
script: "42 * 1337"

# A list of smaller scripts
script:
-	"42 * 1337"
- 	"player.giveItem(mmoitems('MISC', 'GRASSCUTTER'))"

# Conditional
script:
-	if: expression...
	onTrue: script when expression returns value that isn't 0
	onFalse: script when expression returns value that is 0

# Mixed
script:
-	if: stamina.tryTaking(trigger.walkDistance * 0.5)
	onTrue:
	-	player.giveItem(items.STONE)
	-	leveling.addExp(trigger.walkDistance)
	onFalse: player.giveItem(items.DIAMOND)
```

## Trigger types

### Trigger type: `walk`

Triggers when player is moving

- `trigger.walkDistance`: The player's travelling distance, measured in blocks.

### Trigger type: `release`

Triggers when player is releasing the pet to the wild
