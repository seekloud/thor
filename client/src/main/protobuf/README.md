#esheep API设计文档 2019.1.3


### 概述
1. 序列化使用protobuf
2. 通信服务使用gRPC


### 基础概念
1. room: 一个模拟环境，同一个room下的player在同一个模拟环境内进行比赛；
2. player: 参与虚拟空间进行比赛的角色;
3. action: player可以使用行为;
4. observation: 可以获取到的包括所有player的模拟环境信息;
5. information: 分数，击杀，死亡等信息;
6. reward: 一个分数奖励;
7. 不可变元素: 位置、形状、颜色、行为都不会发生变化的元素
8. 可变元素: 位置、形状、颜色、行为中一项或多项会发生变化的元素
9. 玩家实体：玩家直接操控的角色
10. 所有权: 元素可以是中立，也可以属于某个玩家;
11. episode:  


### 主要流程
1. 创建房间
2. 加入房间
3. 提交action(s)
4. 获得observation
5. 获取information
6. 死亡后复活
7. 退出



### 动作
1. 移动方向（move）
    * 一共八种输入：←↑→↓↖↙↗↘
    * 八选一；
    * 表示键盘的方向按键；
2. 鼠标指针移动（swing）
    * 一个二元向量(r，d)
    * r表示朝向：\(-pi，pi\]
    * d表示距离：\[0, Dmax\], Dmax为预先定义好的移动最大距离；
    * 表示以鼠标指针当前位置为起点，按照朝着r方向，移动距离d；
3. 激发（fire）
    * 一个状态量，
    * 取值范围0，1，2，3，4，等等；
    * 0表示无激发；
    * 表示攻击，投掷，加速等等主要的激发操作；
    * 通常对应于鼠标左键，右键，键盘空格等主操作；
4. 操纵（apply）
    * 一个状态量
    * 取值范围0，1，2，3，4，等等；
    * 0表示不使用功能；
    * 表示，分裂，喂食，使用技能，使用道具，等辅助功能操作；
    * 通常对应于键盘的E，F，1，2，3，4等辅助操作；


### 观察数据 (Observation)
1. 分层视图  

    1.  视野在整个地图中的位置 (location)         
        * 整个矩形图像表示全部可以被观测区域    
        * 当前屏幕观测区域用矩形方块表示    
        * 无任何其他元素呈现    
        * 背景使用纯黑    
        * 颜色含义: 当前观察区域用纯白    

    2.  视野内的不可变元素  (immutable element)  
        * 位置不会改变      
        * 形状不会改变    
        * 颜色不会改变    
        * 行为不会改变    
        * 例如：地图边界，障碍物    
        * 背景使用纯黑    
        * 颜色无特定要求，与物体概念一致即可    
        
    3. 视野内的可变元素  (mutable element)  
        * 位置、形状、颜色、行为一项或多项有可能发生变化    
        * 例如：物品、道具、子弹、轨迹等      
        * 背景使用纯黑      
        * 颜色无特定要求，与物体概念一致即可    
        
    4. 视野内的玩家实体 (bodies)    
        * 各个参与游戏的玩家代表，直接反映用户的操作    
        * 满足稳定性，即，不可随意变化    
        * 满足可操作性，即，用户可以直接控制    
        * 例如：角色自身，圈地游戏中的头部方块    
        * 背景使用纯黑    
        * 颜色含义：    
        * 每个玩家的操控核心的颜色用玩家色值表示    
        
    5. 视野内的所有权视图 (asset ownership)   
        * 绘制出所有包含玩家从属关系的元素    
        * 绘制出所有玩家实体    
        * 背景使用纯黑    
        * 颜色含义：所有绘制内容的颜色与所属玩家的玩家色值相同    
        
    6. 视野内的当前玩家资产视图 (self asset)  
        * 绘制出当前玩家的操控核心    
        * 绘制出当前玩家所拥有的元素    
        * 背景使用纯黑    
        * 颜色无特定要求，与物体概念一致即可    
        
    7. 鼠标指针位置 (pointer)  
        * 绘制出鼠标指针在当前视野中的位置    
        * 背景使用纯黑    
        * 鼠标用圆形表示，默认为白色
        * 可以使用不同的颜色表示鼠标不同的状态       
             
    8. 当前用户状态视图  (self status)  
        * 采用直方图的方式，表现用户的各项指标    
        * 例如：速度，长度，体积，攻击力，防御力    
        * 背景使用纯黑    
        * 颜色无特定要求     

        
2. 人类视图


### 互动信息 (Information)
1. 击杀
2. 得分
3. 血量




### sbt 配置 (可以参考本工程的配置)
1. 在project目录下创建文件:scalapb.sbt
2. 编辑文件scalapb.sbt内容为:
```
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.19")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.8.2"
```
3. 在build.sbt文件中增加以下依赖:
```
        "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
        "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
```
4. 在build.sbt文件的工程设置中增加:
```
      PB.targets in Compile := Seq(
        scalapb.gen() -> (sourceManaged in Compile).value
      )
```
5. 在`src/main/protobuf`路径下编写*.proto文件;
6. 进入sbt交互模式，输入 `protocGenerate` 即可生成proto文件对应的scala文件；
7. 生成的文件在 `./target/scala-2.12/src_managed/main`，这些生成的文件，无需拷贝到src目录下，在项目代码中直接就可以使用;



### Copyright and License Information

---
Copyright 2018 seekloud (https://github.com/seekloud)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.







