# Crypto
Java package containing two modules:

1. **encryption**: RSA/AES encryption, RSA keys loading from resources, RSA with SHA-256 message signing and checking, AES key generator, SHA-256 hash
2. **secure_tcp**: secure TCP connection, SecureTCPServer, SecureTCPClient

## 1. Encryption

### Steps to generate and read RSA keys into your JAVA application:

1. In Maven project pom.xml file add resource directory as below (filtering false is important to tell maven to not change the files while building project):
```xml
<build>
  <resources>
      <resource>
          <directory>src/main/java/res</directory>
          <filtering>false</filtering>
      </resource>
  </resources>
</build>
```
2. In resource directory run generate_rsa_keys.sh script. This script will generate three files: private_key.pem, private_key.der and public_key.der. We will be using private_key.der and public_key.der files to read private and public key into our JAVA application so you can move private_key.pem somwhere else on your computer (but you should keep it cause it contains informations about your private-public key). According to what you want you should delete private_key.der from resource directory if application will use only public key. Public key is always needed.
3. Now in your application you can read keys from resource directory to RSAKeyContainer class as follows:
```java
String privateKeyResourceName = "/private_key.der";
String publicKeyResourceName = "/public_key.der";

RSAKeyContainer instance = 
        new RSAKeyContainer(publicKeyResourceName, privateKeyResourceName);
```

### Tutorial how to use Crypto.encryption

Using this module is very simple. Below are examples for each class from this module. Remember that you need to add private and public key to your project resources in .der format, as explained in previous chapter.

For now if you want to use Crypto in your maven project, you can go to pom.xml file and paste following things:
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.bercik</groupId>
    <artifactId>Crypto</artifactId>
    <version>v0.1</version>
  </dependency>
</dependencies>
```

What this is doing is downloading and building Crypto package in your project.

Function that asserts that two byte arrays are the same using to test our code:
```java
public static boolean assertArrayEquals(byte[] array1, byte[] array2)
{
  if (array1.length != array2.length)
  {
    return false;
  }

  for (int i = 0; i < array1.length; ++i)
  {
    if (array1[i] != array2[i])
    {
      return false;
    }
  }

  return true;
}
```

#### RSA
```java
public static void RSATest() throws Exception
{
  // instance of RSA class
  RSA rsa = new RSA();
  // rsa key container class
  RSAKeyContainer rsakc = new RSAKeyContainer("/public_key.der", 
          "/private_key.der");
  // some data you want to encrypt
  byte[] data = new byte[] { 1, 2, 3, 4 };
  // encrypt data using server public key
  byte[] encrypted = rsa.encrypt(data, rsakc.getPublicKey());
  // decrypt encrypted data
  // remember that if you decrypting data using private key, you need
  // to call constructor from RSAKeyContainerClass whitch gets public and
  // private key resource name as input, otherwise you will get 
  // NullPointerException when trying to decrypt data using null private
  // key
  byte[] decrypted = rsa.decrypt(encrypted, rsakc.getPrivateKey());
  
  // check if data and decrypted arrays are the same
  System.out.println(assertArrayEquals(data, decrypted));
}
```
