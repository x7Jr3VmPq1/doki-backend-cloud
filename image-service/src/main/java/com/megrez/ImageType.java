package com.megrez;

public enum ImageType {
    USER_AVATAR("avatar"), VIDEO_COVER("cover"),COMMENT_IMG("comment");

    private final String dir;

    ImageType(String dir) {
        this.dir = dir;
    }

    // 根据字符串获取枚举，找不到抛出异常
    public static ImageType fromString(String type) {
        for (ImageType t : ImageType.values()) {
            if (t.dir.equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new RuntimeException("不合法的路径");
    }
}
