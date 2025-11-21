package fr.dreamin.dreamapi.core.utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Pair<A, B> {
    
    private A first;
    private B second;
    
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

}
