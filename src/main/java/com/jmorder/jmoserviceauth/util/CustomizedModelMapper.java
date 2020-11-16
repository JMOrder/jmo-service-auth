package com.jmorder.jmoserviceauth.util;

import org.modelmapper.ModelMapper;

public class CustomizedModelMapper extends ModelMapper {
    @Override
    public <D> D map(Object source, Class<D> destinationType) {
        if(source == null){
            return null;
        }

        return super.map(source, destinationType);
    }
}
