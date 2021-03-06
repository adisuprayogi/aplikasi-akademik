package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Akses;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Dosen;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresensiDto {
    private String id;
    private String namaMatkuliah;
    private String namaKelas;
    private String namaDosen;
    private int jumlahSks;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamMulai;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamSelesai;
    private Akses akses;
    private String ruangan;
    private Set<Dosen> dosens = new HashSet<>();
}
