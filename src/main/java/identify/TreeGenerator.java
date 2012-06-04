package identify;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import javax.naming.NamingException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class TreeGenerator {
    private final static String PROP_SUBPATH = "tree.subpath";
    private final String outputSubPath;
    private final File fullDir;
    private final File outputDir;
    private final File fullVCardFile;
    private final UserInfoFactory certInfoFactory;
    private final X509CertificateFactory x509Factory;

    public TreeGenerator(final ConfigPropertiesFactory configPropertiesFactory,
                         final UserInfoFactory certInfoFactory,
                         final X509CertificateFactory x509Factory,
                         final String path) throws ConfigProperties.ConfigLoadingException {
        this.certInfoFactory = certInfoFactory;
        this.x509Factory = x509Factory;

        fullDir = new File(path);
        final String dateFormat = configPropertiesFactory.get(ConfigProperties.Domain.TREE).get(PROP_SUBPATH).toString();
        outputSubPath = new SimpleDateFormat(dateFormat).format(new Date());
        outputDir = new File(fullDir, outputSubPath);
        fullVCardFile = new File(outputDir, "everybody.vcf");
    }

    public String generate() throws IOException, NamingException {
        if (!outputDir.mkdirs()) {
            throw new RuntimeException("Destination already exists. We don't want to overwrite generated certificates.");
        }

        fullVCardFile.createNewFile();
        final FileWriter vCardWriter = new FileWriter(fullVCardFile);
        final BufferedWriter vCardBufferedWriter = new BufferedWriter(vCardWriter);

        List<UserInfo> users = certInfoFactory.getUsers();
        for(UserInfo user: users) {
            log.info("Creating certificate for user {}", user);
            try {
                String vCard = user.toVCard();
                vCardBufferedWriter.write(vCard);
                Files.write(vCard, new File(outputDir, String.format("%s.vcf", user.getUid())), Charsets.UTF_8);

                /* Write the vCard first as x509 fails for all non-tech users. */
                String cert = x509Factory.get(user).toPEM();
                Files.write(cert, new File(outputDir, String.format("%s.pem", user.getUid())), Charsets.UTF_8);
            } catch (Exception e) {
                    log.warn("Failure: {}", e.toString());
            }
        }

        vCardBufferedWriter.close();
        return outputSubPath;
    }
}
