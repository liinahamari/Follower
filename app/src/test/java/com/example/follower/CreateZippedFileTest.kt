package com.example.follower

import android.content.Context
import android.os.Build
import android.os.Looper.getMainLooper
import androidx.test.platform.app.InstrumentationRegistry
import com.example.follower.di.modules.DEBUG_LOGS_DIR
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.helper.rx.TestSchedulers
import com.example.follower.screens.logs.CreateZipLogsFileResult
import com.example.follower.screens.logs.LoggerInteractor
import com.example.follower.screens.logs.ZIPPED_LOGS_FILE_NAME
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.*
import java.util.zip.ZipInputStream


private const val LOREM = """
    Lorem ipsum dolor sit amet, persius efficiendi sea an, vim te nusquam luptatum dissentias. Fabulas omittam sed an, eirmod facilisis iudicabit ne vis. Ad quo praesent vituperata adversarium, ea mei quot ullamcorper. Usu quis facilisi et.
Omnium mentitum quaestio et eos, feugait nominavi qui an, tamquam praesent id has. At ius eruditi efficiendi, an assum viris instructior pro. Facer inermis honestatis est eu, mazim eirmod copiosae in cum, ei admodum efficiendi quo. Ut omnesque deleniti nominati vel, salutandi scriptorem in usu. Cu causae consectetuer sit, eos ex utroque consulatu. Ut quo bonorum nostrum, mucius definiebas no mea.
Et everti dissentiet cum, nec eu primis pericula. Maiestatis assueverit vis no. Id eos probatus senserit, has tale probo cu. Est mazim doming causae et, vix ex odio mediocrem.
Ea nonumes mentitum ponderum vel, cum paulo scriptorem ea. Tollit tacimates consectetuer ne vis. His omittam nominati iracundia ei, movet complectitur mel ei. Vis no unum maluisset. Perfecto delicata iudicabit vix te, inermis copiosae rationibus mel an, nam te enim lorem.
Et possit labitur eligendi has, no mutat consul theophrastus vel, quo vidit tincidunt ea. Esse inimicus qui ei. Sale tantas moderatius vim id, usu et summo tibique vivendum. Luptatum nominati ei vel. Ea nam augue virtute fabellas, laudem tractatos pri ei, modus ornatus mei eu.
Sed animal laboramus ex. Ei ius lorem iuvaret qualisque, pertinax vulputate ex ius. Quas tincidunt dissentias ex has, vidit pertinacia omittantur an his. No idque utinam vis, eum ut error quodsi sensibus, in vidit iusto perfecto ius. Quis commune an eos, sed ei ferri falli iudicabit. Ad sea solum voluptua percipit.
Nec ut dolores inciderint, usu et atqui nonumy definiebas, id autem illum eleifend mei. Mei an justo error numquam, ne tation aliquam consequat cum, pro in persius erroribus. Ei usu delenit definitionem, ea has saperet dissentiunt. No vix illud utamur laoreet. Pri at solum exerci vulputate, eos epicurei adversarium ne. Vim detraxit sadipscing ei, vel te solum graece.
Mei id consulatu laboramus. Albucius disputationi nec no, graeci democritum te cum, mea cu omnes sapientem. Eam ea partem integre suscipiantur, atqui assentior id quo. An lucilius facilisis mei. Sea duis option eu, facilisis aliquando no per, nec vide aeterno atomorum at. Ius id alii regione, esse animal appareat ut eos. Sed et everti cetero suavitate, mei ei autem graeci moderatius, id labores quaestio vis.
Cu nisl iudico dolorum vix, mei amet noster cu. Ius ex probatus accusata, quis tota placerat cu sed, qui tation omnesque corrumpit ex. Eos eu alii habemus pericula. Ad saperet copiosae qualisque nec.
Ex omnium iuvaret patrioque vis. Ea pri aliquam nonumes comprehensam, cu nam mutat salutatus, ei qui oratio dissentiet. Ad qui summo eruditi. Wisi idque fierent est et, libris epicurei cum ex. Sumo equidem feugait ex vis, commune singulis no sit.
"""

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class CreateZippedFileTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().context
    private val composers = BaseComposers(TestSchedulers())
    private val logsFile = createTempFile()
    private val logsInteractor = LoggerInteractor(context, FlightRecorder(logsFile, composers), composers, logsFile)

    @Before
    fun `fill File with some noise`() = logsFile.writeText(LOREM)

    @After
    fun `tear down`() {
        logsFile.delete()
    }

    @Test
    fun `create zipped file test`() {
        shadowOf(getMainLooper()).idle()

        val zippedLogsFile = File(File(context.filesDir, DEBUG_LOGS_DIR), ZIPPED_LOGS_FILE_NAME)
        assert(zippedLogsFile.exists().not())

        logsInteractor.createZippedLogsFile()
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it is CreateZipLogsFileResult.Success }

        assert(zippedLogsFile.exists())
        assert(zippedLogsFile.length() > 0)
        assert(zippedLogsFile.length() < logsFile.length())

        println("Original file's size: ${logsFile.length()}")
        println("Zipped file's size: ${zippedLogsFile.length()}")

        with(unzip(zippedLogsFile)) {
            assert(exists())
            assert(readLines().first { it.isNotBlank() }.trim() == LOREM.split("\n").first { it.isNotBlank() }.trim())
        }
    }

    private fun unzip(zipFile: File): File {
        val tempFile = createTempFile()
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            while (zis.nextEntry != null) {
                val baos = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var count: Int
                FileOutputStream(tempFile).use { fout ->
                    while (zis.read(buffer).also { count = it } != -1) {
                        baos.write(buffer, 0, count)
                        val bytes: ByteArray = baos.toByteArray()
                        fout.write(bytes)
                        baos.reset()
                    }
                }
                zis.closeEntry()
            }
        }
        return tempFile
    }
}