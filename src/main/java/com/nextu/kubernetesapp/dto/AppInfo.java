package com.nextu.kubernetesapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AppInfo {
    private final String appName;
    private final String hostName;
    private final String os;
}
