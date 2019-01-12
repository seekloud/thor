package org.seekloud.thor.front

import mhtml.Var
import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.pages.MainPage
import org.seekloud.thor.front.utils.{Http, JsFunc}
import org.seekloud.thor.shared.ptcl.TestPswRsp
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import io.circe.syntax._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
  * User: Taoz
  * Date: 6/3/2017
  * Time: 1:03 PM
  */
@JSExportTopLevel("front.Main")
object Main {

  val guestName = List("安琪拉","白起","不知火舞","妲己","狄仁杰","典韦","韩信","老夫子","刘邦",
    "刘禅","鲁班七号","墨子","孙膑","孙尚香","孙悟空","项羽","亚瑟","周瑜",
    "庄周","蔡文姬","甄姬","廉颇","程咬金","后羿","扁鹊","钟无艳","小乔","王昭君",
    "虞姬","李元芳","张飞","刘备","牛魔王","张良","兰陵王","露娜","貂蝉","达摩","曹操",
    "芈月","荆轲","高渐离","钟馗","花木兰","关羽","李白","宫本武藏","吕布","嬴政",
    "娜可露露","武则天","赵云","姜子牙","哪吒","诸葛亮","黄忠","大乔","东皇太一",
    "庞统","干将莫邪","鬼谷子","女娲","SnowWhite","Cinderella","Aurora","Ariel","Belle","Jasmine",
    "Pocahontas","Mulan","Tiana","Rapunzel","Merida","Anna","Elsa","Moana")

  val version = Var("")

  def getVersion: Unit = {
    Http.getAndParse[TestPswRsp](Routes.getVersion).map{
      rsp =>
        if(rsp.errCode == 0){
          version := rsp.psw
        }
        else JsFunc.alert(s"报错啦$rsp")
    }
  }
  @JSExport
  def run(): Unit = {
    getVersion
    MainPage.show()
  }



}
