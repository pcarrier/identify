package sslify;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
public class CertInfo implements Serializable {
    @NonNull
    private final String cn;
    @NonNull
    private final String uid;
    @NonNull
    private final String mail;
    @NonNull
    private final String[] groups;
}
