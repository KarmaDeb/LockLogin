<?php

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class AddLockLogin extends Migration {

    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up() {
        Schema::table('users', function (Blueprint $table) {
            if (!Schema::hasColumn('users', 'PLAYER')) {
                $table->string('PLAYER')->nullable();
            }
            if (!Schema::hasColumn('users', 'EMAIL')) {
                $table->string('EMAIL')->nullable();
            }
            if (!Schema::hasColumn('users', 'UUID')) {
                $table->string('UUID')->nullable();
            }
            if (!Schema::hasColumn('users', 'PASSWORD')) {
                $table->string('PASSWORD')->nullable();
            }
            if (!Schema::hasColumn('users', 'FAON')) {
                $table->smallInteger('FAON')->default(0);
            }
            if (!Schema::hasColumn('users', 'GAUTH')) {
                $table->string('GAUTH')->nullable();
            }
            if (!Schema::hasColumn('users', 'FLY')) {
                $table->smallInteger('FLY')->default(0);
            }
            if (!Schema::hasColumn('users', 'PIN')) {
                $table->string('PIN')->nullable();
            }
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down() {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['PLAYER', 'FAON', 'GAUTH', 'FLY', 'PIN']);
        });
    }
}
