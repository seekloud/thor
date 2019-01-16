package org.seekloud.thor.shared.ptcl.model

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/14
  * Time: 14:34
  */
object Constants {

  object GameState{
    val firstCome = 1
    val play = 2
    val stop = 3
    val loadingPlay = 4
    val relive = 5
    val replayLoading = 6
  }

  val preExecuteFrameOffset = 2 //预执行2帧

  val fakeRender = false

  val canvasUnitPerLine = 160 //可视窗口每行显示多少个canvasUnit

  val pictureMap = mutable.HashMap[String,String] (
    "background.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/a6875ca5be6915a1f7b73b70df73bf84.png",
    "char1-0.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/7a4f3a04b0ef8078a4cc7a1b28ab4fdd.jpg",
    "char1-1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/0e37e768ec38e18788f669faaaaaed75.png",
    "char1-2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/02ce495df89ec431212eea888e4004a3.png",
    "char1-3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/bde730c065a4ea618cb54e4f5507ac5f.png",
    "char2-0.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/45799858226e66c7f843b6067a699af4.png",
    "char2-1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/54a77ac774c90d94a575975219e6d698.png",
    "char2-2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/8309d28fd2eb6bc79c64be88d1544cee.png",
    "char2-3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/39e0ea594af50f7b8bb5008379ec9361.png",
    "char3-0.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/7fa94cb16e2c822ff20bff1c7de98564.png",
    "char3-1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/a328ced7d40b15b053ff260eeecbc5f5.png",
    "char3-2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/50c2f1312602089d79d35ed7bcc2fdaa.png",
    "char3-3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/25820695e15c3889104a9d19a6867496.png",
    "char4-0.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/2757a5a3e99e198b78bebb2b46692c62.png",
    "char4-1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/e8c2a999c4c8c7ba6990cd7de04dcf7d.png",
    "char4-2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/79f6896bc992406731fba3180da90305.png",
    "char4-3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/c130bfb9dc9ecb1ff1bad4fad9d925e6.png",
    "char5-0.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/c7be581ecee640fe9c83ddb1f491c9d5.png",
    "char5-1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/d651fe15a0d50ab8eb19b18bcb25b5de.png",
    "char5-2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/95b133064e12b4497cdc282e10c8781f.png",
    "char5-3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/af01c6ebbbed37d4a501f0a40ed37260.png",
    "food-sheet0-0.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/898027aecfe0ca4bc5efba1a8d86c801.png",
    "food-sheet0-1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/694804e8125972d6d64cd4ddbf302d9f.png",
    "food-sheet0-2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/d661228afb66f116c96467854e1003ca.png",
    "food-sheet0-3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/b1198b84f1cace1fcfa103ec4aa27855.png",
    "food-sheet0-4.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/38d2abb409df3af9fbf44b0918b06468.png",
    "food-sheet0-5.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/d677ddb2ea2dbdfe544c86d5a50e184f.png",
    "food-sheet0-6.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/dbe4b2d13c69cd146b369ee4808caf97.png",
    "food-sheet0-7.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/d49496ba3b0b11b4d4364bdde831e931.png",
    "food-sheet2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/08fcf5555e9456dcd45a4c8c3aabb8a9.png",
    "kill1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/b90f1135fc5f06159617ed3e0a600a90.png",
    "kill2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/dbdef8583471cc43a0604a433b8e5349.png",
    "kill3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/81e6c886f6ae9119123c68e9b10ec48a.png",
    "kill4.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/776a722133f98c631488564b42068ea5.png",
    "kill5.png"-> "http://pic.neoap.com/hestia/files/image/OnlyForTest/df7cc293785885d3cef428f0d6e1761f.png",
    "kill6.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/8f12d06293d34074eb9c1acdf97b8c38.png",
    "level-up.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/05c3aa1d961e6408771fc45244b0a95a.png",
    "map.jpg" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/c58fbc5d680190cddcb28ab404df0aff.jpg",
    "speedParticles.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/a2f0d465a812ee61c80e3f241df0808c.jpg",
    "weapon1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/ea08803dc2d211afd9f083f041104bb1.jpg",
    "weapon2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/3e2006fcfd8765b7536c02f92a3a750d.jpg",
    "weapon3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/0def97f7b8ac0157c3039e51ad2d8254.jpg",
    "weapon4.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/c84f557a9efd8af1e2b9610e999a524c.jpg",
    "weapon5.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/291637879e1b6ed16f3aa12dd379e624.jpg",
    "weapon6.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/557540800005e6898235306e1ca00741.jpg",
    "bar.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/67be696df91efc95cd255b7615074b6a.jpg",
    "fillBar.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/e5bc1bca204de6e5f7060d36a25b6547.jpg"
  )



}
