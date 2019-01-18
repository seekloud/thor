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
    "char2-0.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/9190661ca13053618e94eeedcf046b8b.png",
    "char2-1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/e2c623e55508194b0919ae54dbd69ffc.png",
    "char2-2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/c2c14b1ee3e2ed722c6c8dca08af0680.png",
    "char2-3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/7d84bc6e01da18a9e0921cc1552d55c1.png",
    "char3-0.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/97b7d89ee84ddce45b326edae5e99cb3.png",
    "char3-1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/ba63d6885e4518d76948416df2e7d5ca.png",
    "char3-2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/51d05d271fe8cb8cb9cd15c31319ec5c.png",
    "char3-3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/62c16340cbb1e9d02b5230acbd6a7026.png",
    "char4-0.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/2a797fa717d1c36c7ffabe0088e34102.png",
    "char4-1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/8e38368bb164d5cbadcddefc5a267d74.png",
    "char4-2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/21159b79d951061cb4dd6fa486ec84d2.png",
    "char4-3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/08cc5312f396e063d7be6a49eaa41d39.png",
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
    "level-up.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/572a2307c9021cad15a6f007a212bd81.png",
    "map.jpg" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/c58fbc5d680190cddcb28ab404df0aff.jpg",
    "speedParticles.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/a2f0d465a812ee61c80e3f241df0808c.jpg",
    "weapon1.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/ea08803dc2d211afd9f083f041104bb1.jpg",
    "weapon2.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/3e2006fcfd8765b7536c02f92a3a750d.jpg",
    "weapon3.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/0def97f7b8ac0157c3039e51ad2d8254.jpg",
    "weapon4.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/c84f557a9efd8af1e2b9610e999a524c.jpg",
    "weapon5.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/291637879e1b6ed16f3aa12dd379e624.jpg",
    "weapon6.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/557540800005e6898235306e1ca00741.jpg",
    "bar.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/67be696df91efc95cd255b7615074b6a.jpg",
    "fillBar.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/e5bc1bca204de6e5f7060d36a25b6547.jpg",
    "hammer.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/d714fb295524b0f0bed6aca94b935b53.png",
    "hand.png" -> "http://pic.neoap.com/hestia/files/image/OnlyForTest/8970e0eb3ae30901488d351953d0df70.png"
  )



}
