## Thor近距离攻防模拟器

### 一、主要功能

- 实现游玩、观战、录像的游戏逻辑
- 实现浏览器端页面渲染
- 实现客户端页面渲染
- 对接esheep平台

### 二、系统架构
![](https://pic.niuapu.com/hestia/files/image/OnlyForTest/4d3c94fe8f962ce6b5c41d8df52fcb42.jpg)

### 三、模块说明

#### shared
  - config
    - ThorGameConfig
      - 用来存储及获取游戏中各项配置的抽象类。提供获取游戏配置的入口函数。
  
  - component
    - Adventurer
      - 人物元素的抽象类
    - Food
      - 食物元素的抽象类
      
  - thor
    - ThorSchema
#### backend
#### frontend
#### client



