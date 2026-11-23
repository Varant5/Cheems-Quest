package com.cheemsquest.persistence;

import com.cheemsquest.engine.world.Direction;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Entity
@Table(name = "location")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "encounter_chance", nullable = false)
    private double encounterChance;

    @ManyToMany
    @JoinTable(
            name = "location_exit",
            joinColumns = @JoinColumn(name = "location_code"),
            inverseJoinColumns = @JoinColumn(name = "target_code"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "direction")
    private Map<Direction, Location> exits = new EnumMap<>(Direction.class);

    @ElementCollection
    @CollectionTable(name = "location_spawn", joinColumns = @JoinColumn(name = "location_code"))
    private List<SpawnEntry> spawns = new ArrayList<>();

    public Optional<Location> exit(Direction direction) {
        return Optional.ofNullable(exits.get(direction));
    }
}
