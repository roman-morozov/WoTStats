package org.rmorozov.wot_stats;

public class ServersChangeHelper {
    public String GetZoneByServerName(String serverName) {
        String zone = ".ru";
        if (serverName.equals("RU")) {
            zone = "ru";
        }
        if (serverName.equals("EU")) {
            zone = "eu";
        }
        if (serverName.equals("US")) {
            zone = "com";
        }
        if (serverName.equals("SEA")) {
            zone = "asia";
        }
        if (serverName.equals("RK")) {
            return "kr";
        }
        return zone;
    }

    public String GetAppIdByServerName(String ServerName) {
        String AppId = "demo";
        if (ServerName.equals("RU")) {
            AppId = "0d794f09cab314a90ddb7ee2cde5802c";
        }
        if (ServerName.equals("EU")) {
            AppId = "0d794f09cab314a90ddb7ee2cde5802c";
        }
        if (ServerName.equals("US")) {
            AppId = "0d794f09cab314a90ddb7ee2cde5802c";
        }
        if (ServerName.equals("SEA")) {
            AppId = "0d794f09cab314a90ddb7ee2cde5802c";
        }
        if (ServerName.equals("RK")) {
            return "0d794f09cab314a90ddb7ee2cde5802c";
        }
        return AppId;
    }
}
