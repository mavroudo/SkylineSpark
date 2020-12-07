class Point (var line: String) extends Serializable{

  private val cols = line.split(",").map(_.trim())
  val values = cols.toList
  
  override def toString: String = {
    val str="Element : "+values.toString()+""
    str
  }

}
