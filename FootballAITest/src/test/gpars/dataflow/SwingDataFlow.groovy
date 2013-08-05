package test.gpars.dataflow

/*
Visual demo of Dataflows that need to processed in a given order -
like for appending - while retrieving the several parts concurrently.
*/


import groovy.swing.SwingBuilder
import groovyx.gpars.GParsPool
import groovyx.gpars.dataflow.Dataflow
import groovyx.gpars.dataflow.Dataflows
import static javax.swing.BorderFactory.createEmptyBorder
import static javax.swing.WindowConstants.EXIT_ON_CLOSE

def rand = new Random()
def values = (1..5).collect { 1 + rand.nextInt(15) }

final Dataflows retrieved = new Dataflows()
def bars = []
def labels = []

final SwingBuilder builder = new SwingBuilder()
builder.build {
    def frame = builder.frame(title: 'Demo', defaultCloseOperation: EXIT_ON_CLOSE, visible: true, location: [80, 80]) {
        panel(border: createEmptyBorder(10, 10, 10, 10)) {
            gridLayout rows: values.size(), columns: 2, hgap: 10, vgap: 10
            values.eachWithIndex {value, index ->
                bars[index] = progressBar(string: value, minimum: 0, maximum: value, stringPainted: true)
                labels[index] = label()
            }
        }
    }
    frame.pack()
}

Dataflow.task {
    def result = ''
    values.eachWithIndex {value, index ->
        builder.edt { labels[index].text = 'Waiting' }
        def part = retrieved[index]
        builder.edt { labels[index].text = 'Appending ' + part}
        sleep 1000
        result <<= part
        builder.edt { labels[index].text = result }
    }
}

GParsPool.withPool() {
    values.eachWithIndexParallel {value, index ->
        for (progress in 1..value) {
            sleep 1000
            builder.edt { bars[index].value = progress }
        }
        retrieved[index] = value + " "
    }
}
