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
      - 用来存储及获取游戏中各项配置的基类。提供获取游戏配置的入口函数。
  
  - component
    - Adventurer
      - 人物元素属性和逻辑基类
    - Food
      - 食物元素属性和逻辑基类
      
  - thor
    - ThorSchema
      - 游戏逻辑基类。处理游戏事件。
    - EsRecover
      - 回滚逻辑实现。
    - ThorSchemaImpl
      - 游戏逻辑的前端实现。
    - ThoSchemaClientImpl
      - 游戏逻辑及渲染的前端实现。
    - draw
      - 该目录下是游戏页面元素的渲染实现。
  - model
    - 该目录下是游戏配置常量及游戏点线元素工具类包。
  - protocol
    - 该目录下是游戏中的消息定义。
  - util
    - 该目录下包含渲染中间件及四叉树实现。
#### backend

  - http
    - HttpService
      - 加入游戏、观战、录像及接口汇总
    - PlatService
      - 平台用户加入游戏
    - ReplayService
      - 获取录像列表
      - 按照时间获取录像列表
      - 按照用户获取录像列表
      - 获取录像中玩家列表
      - 获取指定录像播放进度
    - RoomInfoService
      - 获取房间列表 
      - 获取指定用户所在房间列表
      - 获取房间中的用户列表
  - core
    - game
      - AdventurerServer
        - 人物元素的后端实现
      - ThorGameConfigServerImpl
        - 游戏配置的后端实现
      - ThorSchemaServerImpl
        - 游戏逻辑的后端实现
    - UserManager
      - 管理用户
      - 建立WebSocket流
    - UserActor
      - 接收、分发用户游玩、观战、录像时的消息
    - RoomManager
      - 游戏房间的管理
      - 为加入用户分配房间
    - RoomActor
      - 实例化并定时执行游戏逻辑
      - 游戏及用户数据接收、分发
    - GameRecorder
      - 定是保存游戏数据
    - GameReplay
      - 录像数据的回放
    - EsheepLinkClient
      - 获取并维护esheep平台token
      - 向二sheep平台写入游戏战绩
    - GameRecorderGetter
      - 游戏战绩的条件筛选获取

#### frontend

  - pages
    - MainPage
      - 主页面，负责根据哈希显示不同页面
    - EntryPage
      - 不依赖平台的匿名玩耍入口页面
    - ThorRender
      - 游戏页面
    - WatchRender
      - 观战页面
    - ReplayRender
      - 录像页面
    
  - thorClient
    - GameHolder
      - 游戏逻辑循环执行、用户行为监听、及ws消息收发基类
    - GameHolder4Play
      - 游戏游玩GameHolder实现
    - GameHolder4Replay
      - 游戏回放GameHolder实现
    - GameHolder4Watch
      - 游戏观战GameHolder实现
    - WebSocketClient
      - webSocket相关流程类
  - utils
    - 绘图中间件的Js实现及常见前端工具
    
#### client

  - core
    - LoginActor
      - 登录esheep平台相关流程处理
    - PlayGameActor
      - 连接游戏及游戏逻辑循环控制
    - TokenActor
    - esheep平台token的维护
  - view
    - LoginView
      - 登录页面
    - PlayGameView
      - 游戏页面
  - controller
    - PlayGameController
      - 循环执行游戏逻辑
      - 用户行为监听
      - 游戏消息收发



