import kotlin.math.PI

fun main() {


    // Create circles
    val circle3 = Circle(radius=2.0,color="blue")
    val circle2 = Circle(2.0)
    val circle1 = Circle()

    // Print circles

    println(
        "Circle 1:\n Area: ${circle1.getArea()} \n Circumference: ${circle1.getCircumference()} \n Description: ${circle1.getDescription()} \n Color:${circle1.get_Color()}")

    println(
        "Circle 2:\n Area: ${circle2.getArea()} \n Circumference: ${circle2.getCircumference()} \n Description: ${circle2.getDescription()} \n Color: ${circle2.get_Color()}")

    println(
        "Circle 3:\n Area: ${circle3.getArea()} \n Circumference: ${circle3.getCircumference()} \n Description: ${circle3.getDescription()} \n Color: ${circle3.get_Color()}")


}





class Circle()
{
    // Properties
    var radius: Double= 1.0
    var color: String= "red"

    // Constructors
    constructor(radius: Double): this(){

        this.color="red"
        this.radius=radius
    }

    constructor(radius: Double,color: String) : this() {
        this.radius=radius
        this.color=color
    }

    // Methods
    fun getArea():Double{
        return  PI *radius*radius
    }
    fun getCircumference():Double{
        return 2*PI*radius
    }
    fun getDescription():String{

        return " Radius:${ radius.toString()} Color:${color} "
    }
    fun get_Color():String{
        return color
    }
}
