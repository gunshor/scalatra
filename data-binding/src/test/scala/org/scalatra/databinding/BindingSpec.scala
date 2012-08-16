package org.scalatra
package databinding

import org.scalatra.util.conversion._
import org.scalatra.validation._

import scala.math._
import org.specs2.mutable.Specification
import java.util.Date
import scalaz._
import Scalaz._
import Conversions._
import org.joda.time.{DateTimeZone, DateTime}
import com.fasterxml.jackson.databind.{ObjectMapper, JsonNode}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.node.TextNode
import net.liftweb.json._

class BindingSpec extends Specification {

  implicit val formats: Formats = DefaultFormats

  "A BasicBinding" should {
    "have a name" in {
      Binding[String]("blah").name must_== "blah"
    }
    "begin the building process with a value of None" in {
      newBinding[String].value must beNone
    }
    "begin the building process with empty validators" in {
      newBinding[String].validators must beEmpty
    }
    "allow adding validators" in {
      val b = newBinding[String].validateWith(_ => { case s => s.getOrElse("").success[FieldError] })
      b.validators must not(beEmpty)
    }
    "bind to a string" in {
      val b = newBinding[String]
      b("Hey".some).value must beSome("Hey")
    }
  }

  "A BindingContainer" should {
    "construct containers by name" in {
      val cont = BindingContainer("login", implicitly[TypeConverter[String, String]])
      cont.name must_== "login"
      cont.original must beAnInstanceOf[Option[String]]
    }

    "construct containers by binding" in {
      implicit val conv: TypeConverter[Seq[String], String] = (s: Seq[String]) => s.headOption
      val binding = newBinding[String]
      val cont = BindingContainer(binding, implicitly[TypeConverter[Seq[String], String]])
      cont.name must_== binding.name
      cont.original must beAnInstanceOf[Option[Seq[String]]]
    }

    "bind to the data" in {
      val cont = BindingContainer("login", implicitly[TypeConverter[String, String]])
      cont.name must_== "login"
      cont.original must beAnInstanceOf[Option[String]]
      val bound = cont(Option("joske".asInstanceOf[cont.S]))
      bound.name must_== "login"
      bound.original must_== Some("joske")
      bound.value must_== Some("joske")
    }
    
  }
  
  "A BindingContainerBuilder" should {
    
    "start the build process by taking a Binding[T]" in {
      import BindingSyntax._
      val b = B(asString("login"))
      
      b().binding.name must_== "login"
      
    }
    
    "build a BindingContainer with Map[String, String]" in {
      import util.ParamsValueReaderProperties._
      val builder = B(Binding[String]("login"))
      val bb = builder()
      val container = bb(Map("login" -> "joske"), implicitly[TypeConverter[String, String]])
      container.value must_== Some("joske")
    }
    
    "build a BindingContainer with Map[String, String] and multiple bindings in a Seq" in {
      import util.ParamsValueReaderProperties._
      val builders = Seq(B(Binding[String]("login")), B(Binding[Int]("age")))
      val params = Map("login" -> "joske", "age" -> 25)
      builders foreach { builder => 
        val bind = builder.asInstanceOf[{type Binder}]
      }
//      val container = bb(Map("login" -> "joske"), implicitly[TypeConverter[String, String]])
//      container.value must_== Some("joske")
    }
  }

  "A BoundBinding" should {
    val binding = newBinding[String]

    "forward the binding name" in {
      val b = binding("blah".some)
      b must beAnInstanceOf[BoundBinding[String, String]]
      b.name must_== binding.name
    }

    "forward the validators" in {
      val b = binding("blah".some)
      b must beAnInstanceOf[BoundBinding[String, String]]
      b.validators must_== binding.validators
    }

    "have the bound value" in {
      val b = binding("blah".some)
      b.value must beSome("blah")
    }

    "allow adding validators" in {
      val b = binding("blah".some)
      val validator: Validator[String] = {case s => s.getOrElse("").success[FieldError]}
      b.validateWith(_ => validator).validators.size must_== (binding.validators.size + 1)
    }
  }

  "BindingImplicits" should {

    import BindingImplicits._
    "provide Binding[Boolean]" in {
      testBinding[Boolean](true)
    }

    "provide Binding[Float]" in {
      testBinding[Float]((random * 100).toFloat)
    }

    "provide Binding[Double]" in {
      testBinding[Double]((random * 100))
    }

    "provide Binding[Int]" in {
      testBinding[Int]((random * 100).toInt)
    }

    "provide Binding[Byte]" in {
      testBinding[Byte]((random * 100).toByte)
    }

    "provide Binding[Short]" in {
      testBinding[Short]((random * 100).toShort)
    }

    "provide Binding[Long]" in {
      testBinding[Long]((random * 100).toLong)
    }

    "provide Binding[DateTime] for a ISO8601 date" in {
      testDateTimeBinding(JodaDateFormats.Iso8601)
    }

    "provide Binding[DateTime] for a ISO8601 date without millis" in {
      testDateTimeBinding(JodaDateFormats.Iso8601NoMillis, _.withMillis(0))
    }

    "provide Binding[DateTime] for a HTTP date" in {
      testDateTimeBinding(JodaDateFormats.HttpDate, _.withMillis(0))
    }

    "provide Binding[Date] for a ISO8601 date" in {
      testDateBinding(JodaDateFormats.Iso8601)
    }

    "provide Binding[Date] for a ISO8601 date without millis" in {
      testDateBinding(JodaDateFormats.Iso8601NoMillis, _.withMillis(0))
    }

    "provide Binding[Date] for a HTTP date" in {
      testDateBinding(JodaDateFormats.HttpDate, _.withMillis(0))
    }

  }

//  "JacksonBindingImplicits" should {
//
//    import JacksonBindingImplicits._
//    "provide Binding[Boolean]" in {
//      testJacksonBinding[Boolean](true)
//    }
//
//    "provide Binding[Float]" in {
//      testJacksonBinding[Float]((random * 100).toFloat)
//    }
//
//    "provide Binding[Double]" in {
//      testJacksonBinding[Double](random * 100)
//    }
//
//    "provide Binding[Int]" in {
//      testJacksonBinding[Int]((random * 100).toInt)
//    }
//
//    "provide Binding[Byte]" in {
//      testJacksonBinding[Byte]((random * 100).toByte)
//    }
//
//    "provide Binding[Short]" in {
//      testJacksonBinding[Short]((random * 100).toShort)
//    }
//
//    "provide Binding[Long]" in {
//      testJacksonBinding[Long]((random * 100).toLong)
//    }
//
//    "provide Binding[DateTime] for a ISO8601 date" in {
//      testJacksonDateTimeBinding(JodaDateFormats.Iso8601)
//    }
//
//    "provide Binding[DateTime] for a ISO8601 date without millis" in {
//      testJacksonDateTimeBinding(JodaDateFormats.Iso8601NoMillis, _.withMillis(0))
//    }
//
//    "provide Binding[DateTime] for a HTTP date" in {
//      testJacksonDateTimeBinding(JodaDateFormats.HttpDate, _.withMillis(0))
//    }
//
//    "provide Binding[Date] for a ISO8601 date" in {
//      testJacksonDateBinding(JodaDateFormats.Iso8601)
//    }
//
//    "provide Binding[Date] for a ISO8601 date without millis" in {
//      testJacksonDateBinding(JodaDateFormats.Iso8601NoMillis, _.withMillis(0))
//    }
//
//    "provide Binding[Date] for a HTTP date" in {
//      testJacksonDateBinding(JodaDateFormats.HttpDate, _.withMillis(0))
//    }
//
//  }
//
//  "LiftJsonBindingImplicits" should {
//
//    val imports = new LiftJsonBindingImports
//    import imports._
//    "provide Binding[Boolean]" in {
//      testLiftJsonBinding[Boolean](true)
//    }
//
//    "provide Binding[Float]" in {
//      testLiftJsonBinding[Float]((random * 100).toFloat)
//    }
//
//    "provide Binding[Double]" in {
//      testLiftJsonBinding[Double](random * 100)
//    }
//
//    "provide Binding[Int]" in {
//      testLiftJsonBinding[Int]((random * 100).toInt)
//    }
//
//    "provide Binding[Byte]" in {
//      testLiftJsonBinding[Byte]((random * 100).toByte)
//    }
//
//    "provide Binding[Short]" in {
//      testLiftJsonBinding[Short]((random * 100).toShort)
//    }
//
//    "provide Binding[Long]" in {
//      testLiftJsonBinding[Long]((random * 100).toLong)
//    }
//
//    "provide Binding[DateTime] for a ISO8601 date" in {
//      testLiftJsonDateTimeBinding(JodaDateFormats.Iso8601)
//    }
//
//    "provide Binding[DateTime] for a ISO8601 date without millis" in {
//      testLiftJsonDateTimeBinding(JodaDateFormats.Iso8601NoMillis, _.withMillis(0))
//    }
//
//    "provide Binding[DateTime] for a HTTP date" in {
//      testLiftJsonDateTimeBinding(JodaDateFormats.HttpDate, _.withMillis(0))
//    }
//
//    "provide Binding[Date] for a ISO8601 date" in {
//      testLiftJsonDateBinding(JodaDateFormats.Iso8601)
//    }
//
//    "provide Binding[Date] for a ISO8601 date without millis" in {
//      testLiftJsonDateBinding(JodaDateFormats.Iso8601NoMillis, _.withMillis(0))
//    }
//
//    "provide Binding[Date] for a HTTP date" in {
//      testLiftJsonDateBinding(JodaDateFormats.HttpDate, _.withMillis(0))
//    }
//
//  }

  "Defining validations" should {
    import BindingImplicits._
    "have a validation for notBlank" in {
      val field = newBinding[String].notBlank
      field.validators must not(beEmpty)
      field.validators.head.apply(Some("hello")).isSuccess must beTrue
      field.validators.head.apply(Some("")).isSuccess must beFalse
      field.validators.head.apply(None).isSuccess must beFalse
    }

    "have a validation for greater than" in {
      val field = newBinding[Int].greaterThan(6)
      field.validators must not(beEmpty)
      field.validators.head.apply(Some(7)).isSuccess must beTrue
      field.validators.head.apply(Some(6)).isSuccess must beFalse
      field.validators.head.apply(Some(1)).isSuccess must beFalse
    }

    "have a validation for non empty collection" in {
      val field = newBinding[Seq[String]].notEmpty
      field.validators must not(beEmpty)
      field.validators.head.apply(Some(Seq("hello"))).isSuccess must beTrue
      field.validators.head.apply(Some(Seq.empty[String])).isSuccess must beFalse
    }

    "allow chaining validations" in {
      val field = newBinding[String].notBlank.validForFormat("""\w+""".r).minLength(6)
      field.validators must haveSize(3)
    }
  }

  def testDateTimeBinding(format: JodaDateFormats.DateFormat, transform: DateTime => DateTime = identity)(implicit mf: Manifest[DateTime], converter: TypeConverter[String, DateTime]) = {
    val field = newBinding[DateTime]
    field.value must beNone
    val v = transform(new DateTime(DateTimeZone.UTC))
    val s = v.toString(format.dateTimeFormat)
    field(Some(s)).value must beSome(v)
  }
  def testDateBinding(format: JodaDateFormats.DateFormat, transform: DateTime => DateTime = identity)(implicit mf: Manifest[Date], converter: TypeConverter[String, Date]) = {
    val field = newBinding[Date]
    field.value must beNone
    val v = transform(new DateTime(DateTimeZone.UTC))
    val s = v.toString(format.dateTimeFormat)
    field(Some(s)).value must beSome(v.toDate)
  }
  def testBinding[T](value: => T)(implicit mf: Manifest[T], converter: TypeConverter[String, T]) = {
    val field = newBinding[T]
    field.value must beNone
    val v = value
    field(Some(v.toString)).value must beSome(v)
  }

//  val jsonMapper = new ObjectMapper()
//  jsonMapper.registerModule(DefaultScalaModule)
//  def testJacksonBinding[T](value: => T)(implicit mf: Manifest[T], converter: TypeConverter[JsonNode, T]) = {
//    val field = newBinding[T]
//    field.value must beNone
//    val v = value
//
//    field(Some(jsonMapper.readValue(jsonMapper.writeValueAsString(v), classOf[JsonNode]))).value must beSome(v)
//  }
//
//  def testJacksonDateTimeBinding(format: JodaDateFormats.DateFormat, transform: DateTime => DateTime = identity)(implicit mf: Manifest[DateTime], converter: TypeConverter[JsonNode, DateTime]) = {
//    val field = newBinding[DateTime]
//    field.value must beNone
//    val v = transform(new DateTime(DateTimeZone.UTC))
//    val s = v.toString(format.dateTimeFormat)
//    field(Some(new TextNode(s).asInstanceOf[JsonNode])).value must beSome(v)
//  }
//
//  def testJacksonDateBinding(format: JodaDateFormats.DateFormat, transform: DateTime => DateTime = identity)(implicit mf: Manifest[Date], converter: TypeConverter[JsonNode, Date]) = {
//    val field = newBinding[Date]
//    field.value must beNone
//    val v = transform(new DateTime(DateTimeZone.UTC))
//    val s = v.toString(format.dateTimeFormat)
//    field(Some(new TextNode(s).asInstanceOf[JsonNode])).value must beSome(v.toDate)
//  }
//
//
//  def testLiftJsonBinding[T](value: => T)(implicit mf: Manifest[T], converter: TypeConverter[JValue, T]) = {
//    val field = newBinding[T]
//    field.value must beNone
//    val v = value
//
//    field(Some(Extraction.decompose(v))).value must beSome(v)
//  }
//
//  def testLiftJsonDateTimeBinding(format: JodaDateFormats.DateFormat, transform: DateTime => DateTime = identity)(implicit mf: Manifest[DateTime], converter: TypeConverter[JValue, DateTime]) = {
//    val field = newBinding[DateTime]
//    field.value must beNone
//    val v = transform(new DateTime(DateTimeZone.UTC))
//    val s = v.toString(format.dateTimeFormat)
//    field(Some(Extraction.decompose(s))).value must beSome(v)
//  }
//
//  def testLiftJsonDateBinding(format: JodaDateFormats.DateFormat, transform: DateTime => DateTime = identity)(implicit mf: Manifest[Date], converter: TypeConverter[JValue, Date]) = {
//    val field = newBinding[Date]
//    field.value must beNone
//    val v = transform(new DateTime(DateTimeZone.UTC))
//    val s = v.toString(format.dateTimeFormat)
//    field(Some(Extraction.decompose(s))).value must beSome(v.toDate)
//  }

  def newBinding[T]: Binding[T] = Binding[T](randomFieldName)

  def randomFieldName = "field_" + random
}