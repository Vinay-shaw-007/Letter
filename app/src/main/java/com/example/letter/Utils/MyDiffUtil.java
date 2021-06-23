package com.example.letter.Utils;

import androidx.recyclerview.widget.DiffUtil;

import com.example.letter.Models.Message;

import java.util.ArrayList;
import java.util.Objects;

public class MyDiffUtil extends DiffUtil.Callback {

    private ArrayList<Message> oldList;
    private ArrayList<Message> newList;

    public MyDiffUtil(ArrayList<Message> oldList, ArrayList<Message> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList!=null ? oldList.size():0;
    }

    @Override
    public int getNewListSize() {
        return newList!=null ?newList.size():0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return Objects.equals(oldList.get(oldItemPosition).getMessageKey(), newList.get(newItemPosition).getMessageKey());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (!Objects.equals(oldList.get(oldItemPosition).getMessageKey(), newList.get(newItemPosition).getMessageKey())){
            return false;
        }else if (!Objects.equals(oldList.get(oldItemPosition).getMessage(), newList.get(newItemPosition).getMessage())){
            return false;
        }else if (!Objects.equals(oldList.get(oldItemPosition).getImageUrl(), newList.get(newItemPosition).getImageUrl())){
            return false;
        }else if (!Objects.equals(oldList.get(oldItemPosition).getSenderId(), newList.get(newItemPosition).getSenderId())){
            return false;
        }else return Objects.equals(oldList.get(oldItemPosition).getReceiverId(), newList.get(newItemPosition).getReceiverId());

    }
}
