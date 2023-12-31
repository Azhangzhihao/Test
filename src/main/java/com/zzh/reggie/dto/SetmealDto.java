package com.zzh.reggie.dto;

import com.zzh.reggie.entity.Setmeal;
import com.zzh.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
