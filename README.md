# Crypto
Java package to RSA/AES encryption, RSA keys loading from files and secure TCP connection

## Steps to generate and read RSA keys into our JAVA application:

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
