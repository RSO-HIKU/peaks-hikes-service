package com.hiku.peaksAndHikesService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;
import javax.annotation.security.DeclareRoles;

@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({"user", "admin"})
@ApplicationPath("/api/peaks-hikes")
public class PeaksAndHikesService extends Application {
}
