package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapJadwalDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class RekapController {
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private KrsDao krsDao;
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private ProdiDao prodiDao;

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("tahun")
    public Iterable<TahunAkademik> tahun() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(StatusRecord.HAPUS);
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatus(StatusRecord.AKTIF);
    }

    @GetMapping("/rekap/aktifasi")
    public void rekapAktifasi(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
                              @RequestParam(required = false) Jadwal matkul,@PageableDefault(size = 10) Pageable pageable) {




        if (tahunAkademik != null) {
            List<Krs> krs = krsDao.findByTahunAkademikAndStatusAndKrsDetailsNotNull(tahunAkademik,StatusRecord.AKTIF);
            List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndHariNotNull(tahunAkademik);
            model.addAttribute("matkul", jadwal);

            List<Krs> krsWithoutDuplicates = krs.stream()
                    .distinct()
                    .collect(Collectors.toList());

            //        convert list to page
            long start = pageable.getOffset();
            int mulai = (int) start;
            long end = (start + pageable.getPageSize()) > krsWithoutDuplicates.size() ? krsWithoutDuplicates.size() : (start + pageable.getPageSize());
            int selesai = (int) end;
            Page<Krs> pages = new PageImpl<Krs>(krsWithoutDuplicates.subList(mulai,selesai), pageable, krs.size());

//            Page<Krs> krsPage = new PageImpl<>(krsWithoutDuplicates, pageable, krs.size());


            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("krs",pages);
            if (matkul != null){
                Page<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatus(matkul,StatusRecord.AKTIF,pageable);
                model.addAttribute("selectedTahun", tahunAkademik);
                model.addAttribute("krs",krsDetail);
                model.addAttribute("selected",matkul);
            }

        }

    }

    @GetMapping("/rekap/sks")
    public void rekapSks(@RequestParam(required = false) Prodi prodi,@PageableDefault(size = Integer.MAX_VALUE) Pageable page,
                         @RequestParam(required = false) TahunAkademik tahun,@RequestParam(required = false) String skripsi,
                         Model model){
                    model.addAttribute("skripsi",skripsi);

            if (prodi != null && tahun != null) {
                if (skripsi == null || skripsi.isEmpty()) {
                    model.addAttribute("selectedProdi", prodi);
                    model.addAttribute("selectedTahun", tahun);

                    Page<RekapSksDto> rekap = krsDetailDao
                            .cariSks(tahun, prodi, StatusRecord.AKTIF, page);

                    Map<String, RekapSksDto> rekapJumlahSks = new LinkedHashMap<>();

                    for (RekapSksDto r : rekap.getContent()) {

                        // hitung total sks
                        RekapSksDto rsks = rekapJumlahSks.get(r.getId());
                        if (rsks == null) {
                            rsks = new RekapSksDto();
                            rsks.setNama(r.getNama());
                            rsks.setNim(r.getNim());
                            rsks.setJumlah(0);
                        }

                        rsks.tambahSks(r.getJumlah());
                        rekapJumlahSks.put(r.getId(), rsks);


                    }


                    model.addAttribute("rekapJumlahSks", rekapJumlahSks);
                }else{
                    model.addAttribute("selectedProdi", prodi);
                    model.addAttribute("selectedTahun", tahun);

                    Page<RekapSksDto> rekap = krsDetailDao
                            .tanpaSkripsi(tahun, prodi, StatusRecord.AKTIF,StatusRecord.NONAKTIF, page);

                    Map<String, RekapSksDto> rekapJumlahSks = new LinkedHashMap<>();

                    for (RekapSksDto r : rekap.getContent()) {
                            // hitung total sks
                            RekapSksDto rsks = rekapJumlahSks.get(r.getId());
                            if (rsks == null) {
                                rsks = new RekapSksDto();
                                rsks.setNim(r.getNim());
                                rsks.setNama(r.getNama());
                                rsks.setJumlah(0);
                            }

                            rsks.tambahSks(r.getJumlah());
                            rekapJumlahSks.put(r.getId(), rsks);

                    }


                    model.addAttribute("rekapJumlahSks", rekapJumlahSks);
                }
            }


    }
}