package licensing

import java.nio.file.{Files, Paths}
import java.util.{Base64, UUID}

object LicenseGenerator {
  private[this] val licenseDir = "./util/licenseGenerator/src/main/resources/licenses/"

  def listLicenses() = {
    val dir = new java.io.File(licenseDir)
    dir.listFiles.filter(_.getName.endsWith(".license")).map(_.getName.stripSuffix(".license")).map(UUID.fromString)
  }

  def getContent(id: UUID) = {
    val filename = id + ".license"
    val file = Paths.get(licenseDir, filename)
    if (Files.exists(file)) {
      Files.readAllBytes(file)
    } else {
      throw new IllegalArgumentException(s"License [$id] does not exist.")
    }
  }

  def loadLicense(id: UUID) = {
    val content = getContent(id)
    val decoded = Base64.getDecoder.decode(content)
    val str = DecryptUtils.decrypt(decoded)
    License.fromString(str)
  }

  def saveLicense(license: License, overwrite: Boolean = false) = {
    val filename = license.id + ".license"
    val file = Paths.get(licenseDir, filename)
    if ((!overwrite) && Files.exists(file)) {
      throw new IllegalArgumentException(s"License already exists for [${license.id}] and cannot be overwritten.")
    } else {
      val encrypted = EncryptUtils.encrypt(license.toString).toArray
      val encoded = Base64.getEncoder.encode(encrypted)
      Files.write(file, encoded)
    }
  }

  def removeLicense(id: UUID) = {
    val filename = id + ".license"
    val file = Paths.get(licenseDir, filename)
    if (Files.exists(file)) {
      Files.delete(file)
    } else {
      throw new IllegalArgumentException(s"No license available for [$id].")
    }
  }
}
