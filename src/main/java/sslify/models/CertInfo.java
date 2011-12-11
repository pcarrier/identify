package sslify.models;

import lombok.Data;
import lombok.NonNull;

@Data
public class CertInfo {
    @NonNull
    private final String cn;
    @NonNull
    private final String uid;
    @NonNull
    private final String mail;
    @NonNull
    private final String[] groups;
}
