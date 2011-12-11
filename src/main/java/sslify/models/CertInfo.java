package sslify.models;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class CertInfo {
    @NonNull
    private String cn;
    @NonNull
    private String uid;
    @NonNull
    private String mail;
    @NonNull
    private String[] groups;

    @Inject
    public CertInfo(@Assisted String user) {}
}
