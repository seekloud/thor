package org.seekloud.thor.shared.ptcl.util

import org.seekloud.thor.shared.ptcl.component.ObjectOfGame
import org.seekloud.thor.shared.ptcl.model

/**
  * User: TangYaruo
  * Date: 2018/11/13
  * Time: 15:57
  *
  * copied from tank
  * 四叉树
  * 存储地图的所有元素
  * 坐标系
  */
object QuadTree {
  final val MAX_OBJECTS = 5
  final val MAX_LEVELS = 5

  final val QuadFirst = 0
  final val QuadSecond = 1
  final val QuadThird = 2
  final val QuadFourth = 3


}

class QuadTree(bounds: model.Rectangle, level: Int = 0) {

  import QuadTree._

  private var objects: List[ObjectOfGame] = List[ObjectOfGame]()
  private var children: List[QuadTree] = Nil

  private val center = (bounds.topLeft + bounds.downRight) / 2


  /**
    * 获取物体所属的象限id
    *
    * @param o 物体元素
    * @return 象限id，如果在边界的话，返回None
    **/
  def getIndex(o: ObjectOfGame): Option[Int] = {
    val rec = o.getObjectRect()
    val onTop = rec.downRight.y <= center.y
    val onBottom = rec.topLeft.y >= center.y
    val onLeft = rec.downRight.x <= center.x
    val onRight = rec.topLeft.x >= center.x
    if (onTop) {
      if (onRight) {
        Some(QuadFirst)
      } else if (onLeft) {
        Some(QuadSecond)
      } else {
        None
      }
    } else if (onBottom) {
      if (onRight) {
        Some(QuadFourth)
      } else if (onLeft) {
        Some(QuadThird)
      } else {
        None
      }
    } else {
      None
    }
  }


  /**
    * 将该节点进行划分
    **/
  private def split(): Unit = {
    if (children.isEmpty) {
      val first = new QuadTree(model.Rectangle(model.Point(center.x, bounds.topLeft.y), model.Point(bounds.downRight.x, center.y)), level + 1)
      val second = new QuadTree(model.Rectangle(bounds.topLeft, center), level + 1)
      val third = new QuadTree(model.Rectangle(model.Point(bounds.topLeft.x, center.y), model.Point(center.x, bounds.downRight.y)), level + 1)
      val fourth = new QuadTree(model.Rectangle(center, bounds.downRight), level + 1)
      children = first :: second :: third :: fourth :: Nil
    }
  }


  def insert(o: ObjectOfGame): Unit = {
    if (this.children.nonEmpty) {
      getIndex(o) match {
        case Some(quadId) => this.children(quadId).insert(o)
        case None => objects = o :: objects
      }
    } else {
      objects = o :: objects
    }

    if (this.children.isEmpty && this.objects.size > MAX_OBJECTS && this.level < MAX_LEVELS) {
      this.split()
      val seq = objects.map(t => (t, getIndex(t)))
      val childrenObjectList = seq.filter(_._2.nonEmpty)
      objects = seq.filter(_._2.isEmpty).map(_._1)
      childrenObjectList.foreach {
        case ((o, id)) =>
          this.children(id.getOrElse(0)).insert(o)
      }
    }
  }


  /**
    * 检索所有可能碰撞的物体
    **/
  def retrieve(o: ObjectOfGame): List[ObjectOfGame] = {
    var result: List[ObjectOfGame] = Nil
    if (this.children.nonEmpty) {
      getIndex(o) match {
        case Some(id) => result = this.children(id).retrieve(o)
        case None =>
          //切割物体
          carve(o).foreach { id =>
            result = this.children(id).retrieve(o) ::: result
          }

      }
    }
    result ::: this.objects
  }

  /**
    * 检索碰撞的物体，并且过滤掉自己本身
    **/
  def retrieveFilter(o: ObjectOfGame): List[ObjectOfGame] = {
    retrieve(o).filter(_.ne(o))
  }


  def isInner(o: ObjectOfGame): Boolean = {
    val rec = o.getObjectRect()
    rec.topLeft > bounds.topLeft && rec.downRight < bounds.downRight
  }


  /**
    * 刷新地图的物体元素，动态调整四叉树的物体元素
    **/
  def refresh(root: QuadTree): Unit = {

    val needToInsertObjectList: List[ObjectOfGame] = this.objects.filterNot(t => isInner(t))
    val innerObject: List[ObjectOfGame] = this.objects.filter(t => isInner(t))
    this.objects = innerObject

    needToInsertObjectList.foreach(t => root.insert(t))

    if (this.children.nonEmpty) {
      val seq = this.objects.map(t => (t, getIndex(t)))
      this.objects = seq.filter(_._2.isEmpty).map(_._1)
      seq.filter(_._2.nonEmpty).foreach {
        case (o, idOpt) =>
          this.children(idOpt.get).insert(o)
      }


    }
    this.children.foreach(t => t.refresh(root))
  }


  /**
    *
    * 解析这个物体跨越了哪几个象限
    *
    * @param o 物体
    * @return 返回跨界的象限id
    **/
  private def carve(o: ObjectOfGame): List[Int] = {
    var result: List[Int] = Nil
    val rec = o.getObjectRect()
    if (rec.downRight.x > center.x && rec.topLeft.y < center.y) {
      result = QuadFirst :: result
    }
    if (rec.topLeft < center) {
      result = QuadSecond :: result
    }
    if (rec.topLeft.x < center.x && rec.downRight.y > center.y) {
      result = QuadThird :: result
    }
    if (rec.downRight > center) {
      result = QuadFourth :: result
    }
    result
  }

  def remove(o: ObjectOfGame): Unit = {
    this.objects = this.objects.filter(_.ne(o))
    if (this.children.nonEmpty) {
      this.children.foreach(t => t.remove(o))
    }
  }

  def updateObject(old: ObjectOfGame, newOb: ObjectOfGame): Unit = {
    remove(old)
    insert(newOb)
  }

  def clear(): Unit = {
    this.objects = Nil
    this.children = Nil
  }

  def contain(o: ObjectOfGame): Boolean = {
    if (this.objects.exists(_.eq(o))) true
    else if (this.children.nonEmpty) {
      this.children.exists(_.contain(o))
    } else false
  }


  def getPath(o: ObjectOfGame): List[Int] = {
    if (this.children.nonEmpty) {
      getIndex(o) match {
        case Some(x) => this.children(x).getPath(o) ::: List(x)
        case None => List(-1)
      }
    } else List(-1)

  }


}
