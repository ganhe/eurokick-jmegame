package test.gpars.dataflow

import static groovyx.gpars.dataflow.Dataflow.task
import groovyx.gpars.dataflow.DataflowVariable
final def x = new DataflowVariable()
final def y = new DataflowVariable()
final def z = new DataflowVariable()

task {
    z << x.val + y.val
    println "Result: ${z.val}"
}

task {
    x << 10
}

task {
    y << 5
}
println "Result: ${z.val}"