package test.compliation


class Greet {
  def salute( person ) { println "Hello ${person.name}!" }
  
    @groovy.transform.CompileStatic
  def welcome( Place location ) { println "Welcome to ${location.state}!" }
}

@groovy.transform.CompileStatic
class Place{
    String state;
}

a = new Greet()
a.salute([name:"Hai"])