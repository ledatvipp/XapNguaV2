


          
# 📖 Hướng dẫn Toàn diện PassivePet Plugin

## 📋 Mục lục
1. [Giới thiệu](#giới-thiệu)
2. [Cấu trúc Project](#cấu-trúc-project)
3. [Cấu hình Cơ bản](#cấu-hình-cơ-bản)
4. [Tạo Pet Mới](#tạo-pet-mới)
5. [Hệ thống Component](#hệ-thống-component)
6. [Hệ thống Expression](#hệ-thống-expression)
7. [Quản lý Items](#quản-lý-items)
8. [Cấu hình Eggs](#cấu-hình-eggs)
9. [Commands và Permissions](#commands-và-permissions)
10. [Troubleshooting](#troubleshooting)

---

## 🎯 Giới thiệu

PassivePet là một plugin Minecraft cho phép người chơi sở hữu và nuôi dưỡng các thú cưng ảo. Plugin sử dụng hệ thống component modular và expression engine mạnh mẽ.

### Tính năng chính:
- 🐾 Hệ thống pets với leveling và evolution
- ⚡ Stamina system với auto-regen
- 🥚 Egg hatching với rarity system
- 🎯 Trigger system cho automation
- 🎨 Custom textures và particles
- 💎 Buff system tích hợp MythicLib

---

## 📁 Cấu trúc Project

```
PassivePet/
├── config.yml          # Cấu hình chính
├── gui.yml            # Giao diện menu
├── lang.yml           # Ngôn ngữ
├── items.yml          # Items (food, evolver, etc.)
├── eggs.yml           # Cấu hình trứng
└── pets/              # Thư mục chứa pets
    ├── example_pet.yml
    ├── nahara.yml
    └── your_pet.yml
```

---

## ⚙️ Cấu hình Cơ bản

### 1. Config.yml
```yaml:config.yml
globalMaxSlots: 1000              # Số slot tối đa toàn server
slotPermission: petstorage.slot.%s # Permission cho từng slot
```

### 2. Permissions
```
# Cơ bản
passivepet.use                    # Sử dụng plugin
passivepet.pets                   # Mở menu pets

# Admin
passivepet.admin.managepet        # Quản lý pets
passivepet.admin.give             # Give pets cho player

# Slots
petstorage.slot.1                 # Slot 1
petstorage.slot.2                 # Slot 2
# ... tương tự cho các slot khác
```

---

## 🐾 Tạo Pet Mới

### Bước 1: Tạo file YAML
Tạo file mới trong thư mục `pets/` với tên `my_pet.yml`:

```yaml:pets/my_pet.yml
# Thông tin cơ bản của pet
general:
   name: <gold><b>🦊 Golden Fox</b></gold>
   texture: https://textures.minecraft.net/texture/YOUR_TEXTURE_URL
   description:
   -  <!i><yellow>Một chú cáo vàng thông minh
   -  <!i><gray>Có khả năng tìm kiếm kho báu
   -  <!i><green>Bonus: +25% luck khi mining

# Hiển thị 3D trong game
display:
   texture: https://textures.minecraft.net/texture/YOUR_TEXTURE_URL

# Cấu hình ấp trứng
hatching:
   defaultRarity: 2  # 0=common, 1=uncommon, 2=rare, 3=epic, 4=legendary

# Hệ thống level
leveling:
   maxLevel: 150
   maxExp: 100 + leveling.level * 75  # EXP cần cho level tiếp theo
   maxEvolution: 8

# Hệ thống thể lực
stamina:
   maxStamina: 200 + leveling.level * 12 + leveling.evolution * 30

# Buff cho người chơi (cần MythicLib)
mythiclibBuffs:
-  stat: LUCK
   type: FLAT
   value: 5 + leveling.evolution * 2
-  stat: MINING_SPEED
   type: RELATIVE
   value: 0.25

# Hành động tự động
trigger:
-  type: interval
   cooldown: 600 - leveling.level * 2  # Giảm cooldown theo level
   precondition: stamina.value > 15
   lore:
   -  "<!i><yellow>🔍 Treasure Hunt <trigger_progressbar:20:'▓'> <gold><trigger_cooldown>"
   -  "<!i><gray>Tìm kiếm kho báu mỗi 10 phút"
   script:
   -  stamina.take(15)
   -  leveling.addExp(25 + leveling.evolution * 5)
   -  if: math.random() < 0.3 + leveling.evolution * 0.1
      onTrue: player.giveItem(items.GOLD_NUGGET.withAmount(1 + leveling.evolution))
   -  if: math.random() < 0.1
      onTrue: player.giveItem(items.EMERALD)

-  type: walk
   script:
   -  if: stamina.tryTaking(trigger.walkDistance * 0.08)
      onTrue: leveling.addExp(trigger.walkDistance * 1.5)

-  type: interact
   cooldown: 300
   lore:
   -  "<!i><gold>💰 Fox Magic <trigger_progressbar:20:'✦'> <yellow><trigger_cooldown>"
   script:
   -  player.giveItem(items.EXPERIENCE_BOTTLE)
   -  leveling.addExp(50)

-  type: release
   script:
   -  player.giveItem(items.GOLD_INGOT.withAmount(2 + leveling.evolution))
   -  if: leveling.evolution >= 5
      onTrue: player.giveItem(items.DIAMOND.withName("<gold>Fox Diamond"))
```

### Bước 2: Thêm vào Eggs
Cập nhật <mcfile name="eggs.yml" path="c:\Users\hp\OneDrive\Desktop\passivepet2\src\main\resources\example\eggs.yml"></mcfile>:

```yaml:eggs.yml
# ... existing eggs ...

rare:
   name: <blue>Rare Egg
   duration: 3h
   rarity: 2
   pets:
   - example_pet
   - nahara
   - my_pet  # Thêm pet mới vào đây
```

---

## 🧩 Hệ thống Component

### 1. General Component
```yaml
general:
   name: "Tên pet (MiniMessage format)"
   texture: "URL texture cho menu"
   description:
   - "Dòng mô tả 1"
   - "Dòng mô tả 2"
```

### 2. Display Component
```yaml
display:
   texture: "URL texture cho thế giới 3D"
   # Tương lai: particles, animations
```

### 3. Leveling Component
```yaml
leveling:
   maxLevel: "expression"     # Level tối đa
   maxExp: "expression"       # EXP cần cho level tiếp theo
   maxEvolution: "expression" # Số lần evolution tối đa
```

**Variables có sẵn:**
- `leveling.exp` - EXP hiện tại
- `leveling.level` - Level hiện tại
- `leveling.evolution` - Evolution hiện tại
- `leveling.maxExp` - EXP tối đa cho level hiện tại
- `leveling.maxLevel` - Level tối đa
- `leveling.maxEvolution` - Evolution tối đa

**Functions:**
- `leveling.setExp(exp)` - Set EXP
- `leveling.addExp(amount)` - Thêm EXP
- `leveling.setLevel(level)` - Set level
- `leveling.setEvolution(evolution)` - Set evolution
- `leveling.evolve()` - Tăng evolution lên 1

### 4. Stamina Component
```yaml
stamina:
   maxStamina: "expression"   # Stamina tối đa
```

**Variables:**
- `stamina.value` - Stamina hiện tại
- `stamina.max` - Stamina tối đa

**Functions:**
- `stamina.set(value)` - Set stamina
- `stamina.add(amount)` - Thêm stamina
- `stamina.take(amount)` - Trừ stamina
- `stamina.tryTaking(amount)` - Thử trừ stamina (return 1 nếu thành công, 0 nếu thất bại)

### 5. Trigger Component
```yaml
trigger:
-  type: "trigger_type"
   cooldown: "expression"      # Thời gian cooldown (ticks)
   precondition: "expression"  # Điều kiện để trigger
   lore:                       # Hiển thị trong menu
   - "Dòng lore 1"
   - "Dòng lore 2"
   script:                     # Script thực thi
   - "expression 1"
   - "expression 2"
```

**Trigger Types:**
- `interval` - Tự động theo thời gian
- `walk` - Khi người chơi di chuyển
- `interact` - Khi right-click pet
- `punch` - Khi left-click pet
- `release` - Khi thả pet
- `takingDamage` - Khi người chơi nhận damage

---

## 🔧 Hệ thống Expression

### Math Functions
```yaml
# Cơ bản
math.pi                    # Số π
math.pow(x, exp)          # x^exp
math.sqrt(x)              # √x
math.min(a, b)            # Giá trị nhỏ nhất
math.max(a, b)            # Giá trị lớn nhất
math.clamp(x, min, max)   # Giới hạn x trong [min, max]
math.random()             # Số ngẫu nhiên 0-1

# Lượng giác (radian)
math.sin(angle)
math.cos(angle)
math.tan(angle)
math.asin(value)
math.acos(value)
math.atan(value)
math.atan2(y, x)

# Logarithm
math.log2(x)
math.log10(x)
```

### Conditional Expressions
```yaml
# If-else trong script
script:
-  if: condition
   onTrue: action_when_true
   onFalse: action_when_false

# Ternary operator
value: condition ? value_if_true : value_if_false

# Ví dụ
script:
-  if: stamina.value > 50
   onTrue:
   -  player.giveItem(items.DIAMOND)
   -  leveling.addExp(100)
   onFalse: player.giveItem(items.COAL)
```

### Item Creation
```yaml
# Vanilla items
items.MATERIAL_NAME                    # Tạo item với amount = 1
items.STONE.withAmount(5)              # 5 stone
items.DIAMOND.withName("<red>Red Diamond")  # Custom name
items.SWORD.withLore("Line 1", "Line 2")   # Custom lore

# MMOItems (nếu có plugin)
mmoitems("TYPE", "ID")                 # Tạo MMOItems
```

### Player Interaction
```yaml
# Trong trigger script
player.giveItem(item)                  # Give item cho player
print("message")                       # Debug message
```

---

## 🍖 Quản lý Items

### Food Items
```yaml:items.yml
foods:
   basicFood:
      type: COOKED_BEEF
      name: <yellow>Pet Food
      lore:
      -  <!i><gray>Basic food for pets
      -  <!i><green>+30 <gray>stamina
      stamina: 30
   
   premiumFood:
      type: GOLDEN_CARROT
      name: <gold>Premium Pet Food
      lore:
      -  <!i><gray>High-quality pet food
      -  <!i><green>+100 <gray>stamina
      -  <!i><yellow>+25 <gray>EXP
      stamina: 100
      exp: 25
      minRarity: 2  # Chỉ dành cho pets rarity >= 2
```

### Evolution Items
```yaml:items.yml
evolver:
   type: PAPER
   name: <light_purple>Evolution Scroll
   lore:
   -  <!i><gray>Evolve your pet
   -  <!i><light_purple>+1 <gray>evolution
   -  <!i><red>Requires level 15+
   requirements:
      minLevel: 15
```

### Hatcher Items
```yaml:items.yml
hatchers:
   timeElixir:
      type: GLASS_BOTTLE
      name: <aqua>Time Elixir
      lore:
      -  <!i><gray>Speed up hatching
      -  <!i><yellow>-2 hours
      duration: 2h
   
   instantHatcher:
      type: DRAGON_BREATH
      name: <red><b>Instant Hatcher</b></red>
      lore:
      -  <!i><gray>Instantly hatch any egg
      duration: instant
```

---

## 🥚 Cấu hình Eggs

```yaml:eggs.yml
common:
   name: <white>Common Egg
   duration: 1h              # Thời gian ấp
   rarity: 1                 # Độ hiếm
   pets:                     # Danh sách pets có thể ra
   - basic_pet
   - common_pet
   dropRates:                # Tỷ lệ drop (optional)
     basic_pet: 70
     common_pet: 30

legendary:
   name: <gold><b>Legendary Egg</b></gold>
   duration: 12h
   rarity: 4
   pets:
   - dragon_pet
   - phoenix_pet
   requirements:             # Yêu cầu để sử dụng (optional)
      playerLevel: 30
      permission: "pets.legendary"
   dropRates:
     dragon_pet: 60
     phoenix_pet: 40
```

---

## 🎮 Commands và Permissions

### Player Commands
```
/pets                          # Mở menu pets
/pets help                     # Hiển thị help
```

### Admin Commands
```
/pets admin give <pet> <player>     # Give pet cho player
/pets admin item food <id> <player> # Give food item
/pets admin item evolver <player>   # Give evolution item
/pets admin item egg <type> <player> # Give egg
/pets admin item hatcher <id> <player> # Give hatcher item
/pets admin reload                  # Reload config
```

### Permissions
```
# Player permissions
passivepet.use                 # Sử dụng cơ bản
passivepet.pets                # Mở menu
petstorage.slot.X              # Slot thứ X (1, 2, 3...)

# Admin permissions
passivepet.admin.*             # Tất cả admin commands
passivepet.admin.managepet     # Quản lý pets
passivepet.admin.give          # Give pets
passivepet.admin.items         # Give items
passivepet.admin.reload        # Reload config

# Special permissions
pets.legendary                 # Sử dụng legendary eggs
pets.mythical                  # Sử dụng mythical eggs
```

---

## 🔧 Troubleshooting

### Lỗi thường gặp

#### 1. Pet không hiển thị
```yaml
# Kiểm tra:
- File YAML có syntax đúng không?
- Texture URL có hợp lệ không?
- Pet có được thêm vào eggs.yml không?
```

#### 2. Expression lỗi
```yaml
# Kiểm tra:
- Syntax expression đúng chưa?
- Variables có tồn tại không?
- Functions có được gọi đúng không?

# Ví dụ sai:
maxExp: leveling.level * 100 +  # Thiếu giá trị sau +

# Ví dụ đúng:
maxExp: leveling.level * 100 + 50
```

#### 3. Trigger không hoạt động
```yaml
# Kiểm tra:
- Precondition có đúng không?
- Cooldown có hợp lý không?
- Script có lỗi syntax không?

# Debug:
trigger:
-  type: interval
   cooldown: 100
   script:
   -  print("Trigger activated!")  # Debug message
```

#### 4. Items không hoạt động
```yaml
# Kiểm tra:
- Item type có đúng không?
- Requirements có được thỏa mãn không?
- Pet có đủ level/rarity không?
```

### Debug Tips

1. **Sử dụng print()** trong script để debug:
```yaml
script:
-  print("Current stamina: " + stamina.value)
-  print("Current level: " + leveling.level)
```

2. **Kiểm tra console** để xem error messages

3. **Test từng component** một cách riêng biệt

4. **Backup config** trước khi thay đổi lớn

---

## 🎯 Ví dụ Pet Hoàn chỉnh

```yaml:pets/advanced_dragon.yml
# Dragon pet với đầy đủ tính năng
general:
   name: <red><b>🐲 Ancient Dragon</b></red>
   texture: https://textures.minecraft.net/texture/dragon_texture
   description:
   -  <!i><gold>Một con rồng cổ đại huyền thoại
   -  <!i><gray>Sở hữu sức mạnh thiêng liêng
   -  <!i><yellow>Bonus: +100% combat EXP
   -  <!i><red>Requires level 50+ to evolve

display:
   texture: https://textures.minecraft.net/texture/dragon_texture

hatching:
   defaultRarity: 4

leveling:
   maxLevel: 500
   maxExp: 200 + math.pow(1.8, leveling.level) * 100
   maxEvolution: 15

stamina:
   maxStamina: 500 + leveling.level * 20 + math.pow(1.5, leveling.evolution) * 100

mythiclibBuffs:
-  stat: ATTACK_DAMAGE
   type: FLAT
   value: 20 + leveling.level * 3 + leveling.evolution * 10
-  stat: MAX_HEALTH
   type: FLAT
   value: 40 + leveling.level * 2
-  stat: MOVEMENT_SPEED
   type: RELATIVE
   value: 0.2 + leveling.evolution * 0.05

trigger:
-  type: interval
   cooldown: 400 - leveling.level * 1.5
   precondition: stamina.value > 30
   lore:
   -  "<!i><red>🔥 Dragon Breath <trigger_progressbar:25:'▓'> <gold><trigger_cooldown>"
   -  "<!i><gray>Breathe fire and gain treasures"
   -  "<!i><yellow>Consumes 30 stamina"
   script:
   -  stamina.take(30)
   -  leveling.addExp(100 + leveling.evolution * 25)
   -  player.giveItem(items.COAL.withAmount(3 + leveling.evolution))
   -  if: math.random() < 0.4 + leveling.evolution * 0.05
      onTrue: player.giveItem(items.GOLD_INGOT.withAmount(1 + leveling.evolution / 3))
   -  if: math.random() < 0.15
      onTrue: player.giveItem(items.DIAMOND.withName("<red>Dragon Diamond"))

-  type: walk
   script:
   -  if: stamina.tryTaking(trigger.walkDistance * 0.03)
      onTrue: leveling.addExp(trigger.walkDistance * (3 + leveling.evolution * 0.5))

-  type: interact
   cooldown: 600
   lore:
   -  "<!i><gold>💎 Dragon's Blessing <trigger_progressbar:20:'✦'> <yellow><trigger_cooldown>"
   script:
   -  leveling.addExp(200 + leveling.evolution * 50)
   -  player.giveItem(items.EXPERIENCE_BOTTLE.withAmount(5))
   -  if: leveling.evolution >= 5
      onTrue: player.giveItem(items.EMERALD.withAmount(2))

-  type: punch
   cooldown: 1200
   lore:
   -  "<!i><red>⚔️ Ancient Power <trigger_progressbar:20:'⚡'> <yellow><trigger_cooldown>"
   script:
   -  leveling.addExp(500 + leveling.evolution * 100)
   -  player.giveItem(items.BLAZE_POWDER.withAmount(5 + leveling.evolution))
   -  if: leveling.evolution >= 10
      onTrue: player.giveItem(items.NETHER_STAR.withName("<light_purple>Dragon Star"))

-  type: release
   script:
   -  player.giveItem(items.DRAGON_EGG.withName("<red>Ancient Dragon Heart"))
   -  player.giveItem(items.DIAMOND.withAmount(10 + leveling.evolution * 3))
   -  player.giveItem(items.EMERALD.withAmount(5 + leveling.evolution * 2))
   -  if: leveling.evolution >= 15
      onTrue: player.giveItem(items.BEACON.withName("<gold>Dragon's Legacy"))
```

---

## 🎉 Kết luận

Với hướng dẫn này, bạn có thể:
- ✅ Hiểu rõ cấu trúc PassivePet
- ✅ Tạo pets mới với đầy đủ tính năng
- ✅ Cấu hình items và eggs
- ✅ Sử dụng expression system hiệu quả
- ✅ Debug và troubleshoot các vấn đề

**Tips cuối:**
1. Luôn backup config trước khi thay đổi
2. Test từng tính năng một cách riêng biệt
3. Sử dụng print() để debug
4. Tham khảo pets có sẵn để học cách cấu hình
5. Cân bằng gameplay để tránh overpowered

Chúc bạn tạo ra những pets tuyệt vời! 🐾
        
