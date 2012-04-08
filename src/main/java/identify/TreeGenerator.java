package identify;

import lombok.extern.slf4j.Slf4j;

import javax.naming.NamingException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class TreeGenerator {
    private final String subPath;
    private final File timeDir;
    private final File fullVCard;

    private UserInfoFactory certInfoFactory;
    private X509CertificateFactory x509Factory;

    public TreeGenerator(UserInfoFactory certInfoFactory, X509CertificateFactory x509Factory, String path) {
        this.certInfoFactory = certInfoFactory;
        this.x509Factory = x509Factory;

        subPath = new SimpleDateFormat(String.format("yyyy%sMM%sdd%sHH%smm%sss", File.separator, File.separator, File.separator, File.separator, File.separator)).format(new Date());
        timeDir = new File(String.format("%s%s%s", path, File.separator, subPath));
        fullVCard = new File(String.format("%s%s%s%sall.vcf", path, File.separator, subPath, File.separator));
    }

    private void writeFile(String user, String extension, String contents) {
        File f = new File(String.format("%s%s%s.%s", timeDir.getAbsolutePath(), File.separator, user, extension));
        FileWriter writer = null;
        try {
            f.createNewFile();
            writer = new FileWriter(f);
            writer.write(contents);
        } catch (Exception e) {
            log.error("{} {}", e.toString(), e.getStackTrace());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.warn("{} {}", e.toString(), e.getStackTrace());
                }
            }
        }
    }

    public String generate() throws IOException, NamingException {
        if (!timeDir.mkdirs()) {
            throw new RuntimeException("Destination already exists. We don't want to overwrite generated certificates.");
        }
        fullVCard.createNewFile();
        final FileWriter vCardWriter = new FileWriter(fullVCard);
        final BufferedWriter vCardBufferedWriter = new BufferedWriter(vCardWriter);

        List<UserInfo> users = certInfoFactory.getUsers();
        for(UserInfo user: users) {
            log.info("Creating certificate for user {}", user);
            try {
                String vCard = user.toVCard();
                vCardBufferedWriter.write(vCard);
                writeFile(user.getUid(), "vcf", vCard);

                /* Write the vCard first as this fails a lot. */
                String cert = x509Factory.get(user).toPEM();
                writeFile(user.getUid(), "pem", cert);
            } catch (Exception e) {
                    log.warn("Skipped: {}", e.toString());
            }
        }

        vCardBufferedWriter.close();
        return subPath;
    }
}
