package com.vintly.domain.img.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImgService {

    List<String> uploadImgList(List<MultipartFile> files, String path);

    void deleteImgList(List<String> imgList);
}
