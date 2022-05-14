package com.game.service;



import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;

import com.game.repository.PlayerSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;


@Service
public class PlayerService {

   private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public boolean checkForNull(Player player){
        return player.getName() != null && player.getTitle() != null && player.getRace() != null && player.getBirthday() != null && player.getProfession() != null;
    }

    public boolean checkParamValid(Player player){
        return !player.getName().isEmpty() && player.getName().length() <= 12
                && player.getTitle().length() <= 30
                && player.getExperience() > 0 && player.getExperience() <= 10000000
                && player.getBirthday().getTime() > 0
                && player.getBirthday().getYear() > 100 && player.getBirthday().getYear() < 1100;
    }

    public Pageable getPageable (Map<String, String> params){
        PlayerOrder playerOrder;
        int pageNumber;
        int pageSize;

        if (params.containsKey("order")){
            playerOrder = PlayerOrder.valueOf(params.get("order"));
        } else {
            playerOrder = PlayerOrder.ID;
        }

        if(params.containsKey("pageNumber")){
            pageNumber = Integer.parseInt(params.get("pageNumber"));
        } else {
            pageNumber = 0;
        }

        if (params.containsKey("pageSize")){
            pageSize = Integer.parseInt(params.get("pageSize"));
        } else {
            pageSize = 3;
        }
        return PageRequest.of(pageNumber, pageSize, Sort.by(playerOrder.getFieldName()));
    }

    public Specification<Player> specificationForCount (Map<String, String> param){
        Specification<Player> specification = Specification.where(null);

        for(Map.Entry<String,String> entry: param.entrySet()){
            String key = entry.getKey();
            String value= entry.getValue();

            if(key.equals("name")){
                specification = specification.and(PlayerSpec.nameContain(value));
            }
            if(key.equals("title")){
                specification = specification.and(PlayerSpec.titleContain(value));
            }
            if(key.equals("race")){
                specification = specification.and(PlayerSpec.raceEquals(Race.valueOf(value)));
            }
            if(key.equals("profession")){
                specification = specification.and(PlayerSpec.professionEquals(Profession.valueOf(value)));
            }
            if(key.equals("after")){
                specification = specification.and(PlayerSpec.dateAfter(Long.valueOf(value)));
            }
            if(key.equals("before")){
                specification = specification.and(PlayerSpec.dateBefore(Long.valueOf(value)));
            }
            if(key.equals("banned")){
                specification = specification.and(PlayerSpec.banned(Boolean.valueOf(value)));
            }
            if(key.equals("minExperience")){
                specification = specification.and(PlayerSpec.findMinExperience(Integer.valueOf(value)));
            }
            if(key.equals("maxExperience")){
                specification = specification.and(PlayerSpec.findMaxExperience(Integer.valueOf(value)));
            }
            if(key.equals("minLevel")){
                specification = specification.and(PlayerSpec.findMinLevel(Integer.valueOf(value)));
            }
            if(key.equals("maxLevel")){
                specification = specification.and(PlayerSpec.findMaxLevel(Integer.valueOf(value)));
            }
        }
        return specification;
    }

    public Page<Player> getPlayersWith(Specification<Player> specification, Pageable pageable) {
        return playerRepository.findAll(specification, pageable);
    }
    public Integer playersCount(Specification<Player> specification) {
        return (int) playerRepository.count(specification);
    }
    public Player updateFields(Player oldPlayer, Player updPlayer){
        if(updPlayer.getName() != null){
            oldPlayer.setName(updPlayer.getName());
        }
        if(updPlayer.getTitle() != null){
            oldPlayer.setTitle(updPlayer.getTitle());
        }
        if(updPlayer.getRace() != null){
            oldPlayer.setRace(updPlayer.getRace());
        }
        if(updPlayer.getProfession() != null){
            oldPlayer.setProfession(updPlayer.getProfession());
        }
        if(updPlayer.getBirthday() != null){
            oldPlayer.setBirthday(updPlayer.getBirthday());
        }
        if(updPlayer.isBanned() != null){
            oldPlayer.setBanned(updPlayer.isBanned());
        }
        if(updPlayer.getExperience() != null){
            oldPlayer.setExperience(updPlayer.getExperience());
        }

        return oldPlayer;
    }
    public Player save(Player player){
        changeLvl(player);
        return playerRepository.save(player);
    }

    private void changeLvl(Player player){
        player.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));

        player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());
    }

    public Optional<Player> findById(Long id){
        return playerRepository.findById(id);
    }

    public boolean isExist(Long id){
        return playerRepository.existsById(id);
    }

    public void deleteById(Long id){
          playerRepository.deleteById(id);
    }

    public boolean isIdValid(Long id){
        return id > 0;
    }
}
