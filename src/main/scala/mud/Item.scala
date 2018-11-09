package mud

case class Item(
  name: String,
  desc: String,
  damage: Int,
  speed: Int) {
  val coin = ("a coin", "Shiny!", 0, 0)
  val stick = ("a stick", "Brown and sticky.", 3, 75)
  val photo = ("a photo of Bob Ross", "How inspirational.", 0, 0)
}