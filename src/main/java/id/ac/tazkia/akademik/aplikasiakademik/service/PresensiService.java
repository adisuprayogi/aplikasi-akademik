package id.ac.tazkia.akademik.aplikasiakademik.service;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service @Transactional
public class PresensiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PresensiService.class);

    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private PresensiDosenDao presensiDosenDao;
    @Autowired
    private SesiKuliahDao sesiKuliahDao;
    @Autowired
    private NotifikasiService notifikasiService;
    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired private JadwalDosenDao jadwalDosenDao;

    public PresensiDosen inputPresensi(Dosen d, Jadwal j, String beritaAcara, LocalDateTime dateTime) {
        PresensiDosen presensiDosen = new PresensiDosen();
        presensiDosen.setDosen(d);
        presensiDosen.setWaktuSelesai(LocalDateTime.of(LocalDate.now(),j.getJamSelesai()));
        presensiDosen.setWaktuMasuk(dateTime);
        presensiDosen.setStatusPresensi(StatusPresensi.HADIR);
        presensiDosen.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        presensiDosen.setJadwal(j);
        presensiDosenDao.save(presensiDosen);

        SesiKuliah sesiKuliah = new SesiKuliah();
        sesiKuliah.setBeritaAcara(beritaAcara == null? "" : beritaAcara.trim());
        sesiKuliah.setJadwal(j);
        sesiKuliah.setPresensiDosen(presensiDosen);
        sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
        sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
        sesiKuliahDao.save(sesiKuliah);

        if (presensiDosen.getWaktuMasuk().toLocalTime().compareTo(presensiDosen.getJadwal().getJamMulai().plusMinutes(15)) >= 0) {
            notifikasiService.kirimNotifikasiTelat(presensiDosen);
        }

        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusAndKrsTahunAkademik(j,StatusRecord.AKTIF,tahunAkademikDao.findByStatus(StatusRecord.AKTIF));

        for (KrsDetail kd : krsDetail){
            PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
            presensiMahasiswa.setStatusPresensi(StatusPresensi.MANGKIR);
            presensiMahasiswa.setSesiKuliah(sesiKuliah);
            presensiMahasiswa.setKrsDetail(kd);
            presensiMahasiswa.setCatatan("-");
            presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
            presensiMahasiswa.setRating(0);
            presensiMahasiswaDao.save(presensiMahasiswa);
        }

        return presensiDosen;
    }

    public PresensiDosen inputPresensi(Dosen d, Jadwal j, LocalDateTime dateTime) {
        return inputPresensi(d, j, null, dateTime);
    }

}
