package com.vintly.infra.vintageimg;

import com.vintly.domain.vintageimg.entity.VintageImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VintageImgJpaRepository extends JpaRepository<VintageImg, Long> {


}
