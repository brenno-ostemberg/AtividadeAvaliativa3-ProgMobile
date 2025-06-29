package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.dao.UsuarioDao;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;
import com.example.atividadeavaliativa2_progmobile.utils.PasswordHasher;

/**
Activity responsável pelo fluxo de cadastro de um novo usuário.
Coleta os dados do formulário, valida as entradas, processa a senha de forma segura (hashing)
e insere o novo usuário no banco de dados.
*/

public class CadastroActivity extends AppCompatActivity {

    // Variáveis de launcher
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galeriaLauncher;

    // Variáveis de interface
    private EditText campoNome;
    private EditText campoNickname;
    private EditText campoEmail;
    private EditText campoSenha;
    private Button buttonSalvar;
    private ImageView imageViewFotoPerfil;
    private Button buttonAdicionarFoto;
    private String caminhoFotoPerfil = null;

    // Variáveis de dados e controle
    private UsuarioDao usuarioDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        setTitle("Cadastro de usuário");

        // 1. Inicializa os componentes de dados e de UI
        inicializarComponentes();

        // 2. Configura o listener de clique para o botão de salvar
        configurarListenerSalvar();

        // 3. Inicializa os launchers para a câmera e a galeria
        inicializarLaunchers();
    }

    private void inicializarComponentes() {
        // Inicializa o DAO
        usuarioDao = AppDatabase.getDatabase(getApplicationContext()).usuarioDao();

        // Conecta as variáveis de UI aos IDs do XML.
        campoNome = findViewById(R.id.campo_nome);
        campoNickname = findViewById(R.id.campo_nickname);
        campoEmail = findViewById(R.id.campo_email);
        campoSenha = findViewById(R.id.campo_senha);
        buttonSalvar = findViewById(R.id.button_salvar);
        imageViewFotoPerfil = findViewById(R.id.image_view_foto_perfil);
        buttonAdicionarFoto = findViewById(R.id.button_adicionar_foto);
    }

    private void configurarListenerSalvar() {
        buttonSalvar.setOnClickListener(v -> cadastrarUsuario());
        buttonAdicionarFoto.setOnClickListener(v -> exibirDialogoEscolhaFoto());
    }

    private void inicializarLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        abrirCamera();
                    } else {
                        Toast.makeText(this, "Permissão da câmera necessária para tirar foto.", Toast.LENGTH_LONG).show();
                    }
                });
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        // Verifica se os extras não são nulos
                        if (extras == null) {
                            Log.e("CadastroActivity", "Extras nulo no resultado da câmera");
                            return;
                        }
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        // Verifica se a imagem não é nula
                        if (imageBitmap != null) {
                            imageViewFotoPerfil.setImageBitmap(imageBitmap);
                            caminhoFotoPerfil = salvarImagemNoArmazenamentoInterno(imageBitmap);
                        }
                    }
                });
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageViewFotoPerfil.setImageURI(uri);
                        try {
                            // Converte a URI em Bitmap para poder salvar uma "cópia"
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            caminhoFotoPerfil = salvarImagemNoArmazenamentoInterno(bitmap);
                        } catch (IOException e) {
                            Log.e("CadastroActivity", "Erro ao converter a URI em Bitmap", e);
                        }
                    }
                });
    }

    private void abrirCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(takePictureIntent);
    }

    private void abrirGaleria() {
        galeriaLauncher.launch("image/*");
    }

    @NonNull
    private String salvarImagemNoArmazenamentoInterno(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, "profile.jpg");

        try (FileOutputStream fos = new FileOutputStream(mypath)) {
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Log.d("CadastroActivity", "Imagem salva com sucesso em: " + mypath.getAbsolutePath());
            return mypath.getAbsolutePath();
        }
        catch (FileNotFoundException e) {
            Log.e("CadastroActivity", "Arquivo não encontrado para salvar a imagem: " + mypath.getAbsolutePath(), e);
            return "";
        }
        catch (IOException e) {
            Log.e("CadastroActivity", "Erro de IO ao salvar a imagem em: " + mypath.getAbsolutePath(), e);
            return "";
        }
        catch (Exception e) {
            Log.e("CadastroActivity", "Erro inesperado ao salvar a imagem em: " + mypath.getAbsolutePath(), e);
            return "";
        }
    }

    private void exibirDialogoEscolhaFoto() {
        String[] opcoes = {"Tirar Foto", "Escolher da Galeria"};
        new AlertDialog.Builder(this)
                .setTitle("Escolha uma foto")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
                    } else {
                        abrirGaleria();
                    }
                })
                .show();
    }

    // Responsável por orquestrar o fluxo do cadastro do usuário
    private void cadastrarUsuario() {
        // 1. Valida os dados do formulário
        if (!validarCampos()) {
            return;
        }

        // 2. Pega os dados validados dos campos
        String nome = campoNome.getText().toString().trim();
        String nickname = campoNickname.getText().toString().trim();
        String email = campoEmail.getText().toString().trim();
        String senha = campoSenha.getText().toString().trim();

        // 3. Processa a senha e cria o objeto Usuario
        String senhaHash = PasswordHasher.hashPassword(senha);
        Usuario novoUsuario = new Usuario(nome, nickname, email, senhaHash, caminhoFotoPerfil);

        // 4. Salva o novo usuário no banco de dados em uma thread separada
        salvarUsuarioNoBanco(novoUsuario);
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(campoNome.getText().toString().trim())) {
            campoNome.setError("Nome é obrigatório");
            campoNome.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(campoNickname.getText().toString().trim())) {
            campoNickname.setError("Nickname é obrigatório");
            campoNickname.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(campoEmail.getText().toString().trim())) {
            campoEmail.setError("E-mail é obrigatório");
            campoEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(campoSenha.getText().toString())) {
            campoSenha.setError("Senha é obrigatória");
            campoSenha.requestFocus();
            return false;
        }
        if (campoSenha.getText().toString().length() < 6) {
            campoSenha.setError("A senha deve ter no mínimo 6 caracteres");
            campoSenha.requestFocus();
            return false;
        }
        return true;
    }

    private void salvarUsuarioNoBanco(final Usuario usuario) {
        executor.execute(() -> {
            try {
                usuarioDao.insereUsuario(usuario);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                Log.e("CadastroActivity", "Erro ao inserir usuário no banco", e);
                runOnUiThread(() -> Toast.makeText(this, "Erro: E-mail ou Nickname já em uso.", Toast.LENGTH_LONG).show());
            }
        });
    }
}
