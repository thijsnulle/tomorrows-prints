package tmrw.utils

import java.io.OutputStream

class TeeOutputStream(
    private val out1: OutputStream,
    private val out2: OutputStream
) : OutputStream() {
    override fun write(b: Int) {
        out1.write(b)
        out2.write(b)
    }

    override fun flush() {
        out1.flush()
        out2.flush()
    }

    override fun close() {
        out1.close()
        out2.close()
    }
}