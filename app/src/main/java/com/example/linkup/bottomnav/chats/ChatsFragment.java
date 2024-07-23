package com.example.linkup.bottomnav.chats;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.linkup.chats.Chat;
import com.example.linkup.chats.ChatsAdapter;
import com.example.linkup.databinding.FragmentChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ChatsFragment extends Fragment {
    private FragmentChatsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        loadChats();

        return binding.getRoot();
    }

    private void loadChats(){
        ArrayList<Chat> chats = new ArrayList<>();

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("Users").child(uid).child("chats").exists()) {
                    String chatsStr = snapshot.child("Users").child(uid).child("chats").getValue(String.class);
                    if (chatsStr != null && !chatsStr.isEmpty()) {
                        String[] chatsIds = chatsStr.split(",");
                        for (String chatId : chatsIds) {
                            DataSnapshot chatSnapshot = snapshot.child("Chats").child(chatId);
                            if (chatSnapshot.exists()) {
                                String userId1 = chatSnapshot.child("user1").getValue(String.class);
                                String userId2 = chatSnapshot.child("user2").getValue(String.class);

                                if (userId1 != null && userId2 != null) {
                                    String chatUserId = (uid.equals(userId1)) ? userId2 : userId1;
                                    String chatName = snapshot.child("Users").child(chatUserId).child("username").getValue(String.class);

                                    if (chatName != null) {
                                        Chat chat = new Chat(chatId, chatName, userId1, userId2);
                                        chats.add(chat);
                                    }
                                }
                            }
                        }
                    }
                }


                if (!chats.isEmpty()) {
                    binding.chatsRv.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.chatsRv.setAdapter(new ChatsAdapter(chats));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to get user chats", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
