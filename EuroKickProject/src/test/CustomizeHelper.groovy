package test

/**
 *
 * @author cuong.nguyenmanh2
 */
println("shirtList=[")
new File("./src/../assets/Textures/shirt").eachFile(){ File aFile->
    if (aFile.name.endsWith(".png")){
        println "\""+aFile.name+"\","
    }
}
println("]")  