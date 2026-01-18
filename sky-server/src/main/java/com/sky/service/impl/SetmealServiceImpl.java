package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO){
        //保存套餐的基本信息，操作setmeal，执行insert操作
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        //保存套餐和菜品的关联关系
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && setmealDishes.size() > 0){
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealId);
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
    @Override
    @Transactional
    public void delete(List<Long> ids){
        //判断当前菜品是否在起售，起售则不能删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new RuntimeException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐
        setmealMapper.deleteByIds(ids);
        //删除套餐和菜品的关联关系
        setmealDishMapper.deleteBySetmealIds(ids);
    }
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO){
        //修改套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        //删除原有菜品
        List<Long> ids= new ArrayList<>();
        ids.add(setmealDTO.getId());
        setmealDishMapper.deleteBySetmealIds(ids);
        //插入新的菜品
        List<SetmealDish> dishes = setmealDTO.getSetmealDishes();
        if(dishes != null && dishes.size() > 0){
            for (SetmealDish dish : dishes) {
                dish.setSetmealId(setmealDTO.getId());
            }
            setmealDishMapper.insertBatch(dishes);
        }
    }
    /**
     * 根据id查询套餐和套餐的菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDishes(Long id){
        //查询套餐的基本信息
        Setmeal setmeal = setmealMapper.getById(id);
        //查询套餐的菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        //组装数据
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }
    @Override
    public void startOrStop(Integer status, Long id){
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);
        setmealMapper.update(setmeal);
    }

}
