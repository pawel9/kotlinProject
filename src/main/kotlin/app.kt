import spark.kotlin.*
import java.sql.DriverManager
import spark.Request
import spark.Response
import org.h2.Driver

fun main() {
    DriverManager.registerDriver(Driver())
    val http: Http = ignite()
    createTable()

    with(http) {
        port(getHerokuPort())
        staticFiles.location("/public")

        get("/show") {
            response.header("Content-Type", "text/html")
            showTable()
        }

        get("/add") {
            add(request, response)
        }
    }
}

fun getHerokuPort(): Int {
    val processBuilder = ProcessBuilder()
    return if (processBuilder.environment()["PORT"] != null) {
        processBuilder.environment()["PORT"]!!.toInt()
    } else 5000
}


fun showTable(): String {
    val conn = DriverManager.getConnection("jdbc:h2:~/test")

    val stmt = conn.createStatement()
    val rs = stmt.executeQuery("SELECT id, doors, damaged, country FROM TBL01")

    val builder = StringBuilder()
    builder.append("""<table cellspacing="0" style="border:1px solid black;">""")
    builder.append("""<tr><th>id</th><th>doors</th><th>damaged</th><th>country</th></tr>""")

    while (rs.next()) {
        builder.append("""
                        <tr>
                        <td style="border:1px solid black;">${rs.getString("id")}</td>
                        <td style="border:1px solid black;">${rs.getString("doors")}</td>
                        <td style="border:1px solid black;">${rs.getString("damaged")}</td>
                        <td style="border:1px solid black;">${rs.getString("country")}</td>
                        </tr>
                    """)
    }

    builder.append("""</table>""")
    conn.close()
    return builder.toString()
}

fun createTable(){
    val conn = DriverManager.getConnection("jdbc:h2:~/test")
    val stmt = conn.createStatement()
    stmt.executeUpdate("CREATE TABLE TBL01(id int primary key auto_increment,doors int not null,damaged boolean not null,country varchar(255) not null);")
}

fun add(request: Request, resposne: Response): String{
    var doors: Int = 0
    var damaged: Boolean = false;
    var country: String = "defaultCountry"


    if(request.queryParams("doors") != null){
        doors = request.queryParams("doors").toInt()
    }

    if(request.queryParams("damaged") == null){
        damaged = false
    }else{
        damaged = true
    }

    if(request.queryParams("country") != null){
        country = request.queryParams("country")
    }

    val conn = DriverManager.getConnection("jdbc:h2:~/test")

    try {

        val stmt = conn.createStatement()
        stmt.execute("INSERT INTO TBL01 (`doors`, `damaged`, `country`) VALUES(${doors}, ${damaged}, '${country}');")
        conn.close()
    } catch (e: Exception) {
        println(e.message)

    }
    return "Data added"
}