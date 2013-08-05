package test.gpars.dataflow
import groovyx.gpars.dataflow.*
import static groovyx.gpars.dataflow.Dataflow.task

final def df = new Dataflows()

task { df.z = df.x + df.y }

task { df.x = 10 }

task { df.y = 5 }

println "Result: ${df.z}"

