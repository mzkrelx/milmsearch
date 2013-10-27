(function($){

$.fn.inPlaceEditor = function( config ){

	var self = this;
	return self.each(function(idx){

	//デフォルトパラメータ設定
		var c = $.extend({
			openEdit : false
		},config);

		//現在確定してる値
		var value;

		//編集フィールド、表示テキスト生成
		var target = self.eq(idx);
		var tagName = target.attr('tagName');

	//二重起動防止
		if(target.data('in-place-editor')) return ;
		target.data('in-place-editor','init');

		if(/INPUT|TEXTAREA/.test(tagName)){

			//TEXTAREA?
			var isTextarea = (tagName == 'TEXTAREA');

			//編集フィールド
			var editor = target;

			//表示用テキストの生成
			var label = $('<a class="editor" href="javascript:void(0)"/>');
			editor.before(label).hide();
		}
		else{
			//表示用テキスト
			var label = target;

			//編集フィールドの生成
			var editor = $('<input/>').val(label.text());
			label.after(editor.hide());
		}

		//取消,確定ボタンの生成
		var cmdSet = 
			$('<span><button class="esc"">Esc</button><button \
				class="save" >Save</button></span>');
		editor.after(cmdSet);

		//TEXTAREA の場合関連パーツを block 要素にする
		if(isTextarea){
			label.css('display','block');
			cmdSet.css('display','block');
		}

		//エディタの表示処理
		var showEditor = function(){
			editor.show().focus().select();
			//cmdSet.show();
			label.hide();
		}

		//エディタの非表示処理
		var hideEditor = function(){
			label.show().focus();
			editor.hide();
			cmdSet.hide();
		}

		//取消処理
		var cancelEdit = function(){
			editor.val(value);
			hideEditor();
		}

		//確定処理
		var saveEdit = function(){
			value = editor.val();

			//textarea の場合
			if(isTextarea){

				//実体参照に置き換える
				var html = value
					.replace(/^\n+|\n+$/g,'')
					.replace(/&/g, '&amp;')
					.replace(/</g, '&lt;')
					.replace(/>/g, '&gt;')
					.replace(/"/g, '&quot;');
				var arr = html.split('\n');

				//一行を P 要素で囲う
				html = '';
				for(var i = 0 ; i < arr.length ; i++ ){
					html += ('<p>' + arr[i] + '</p>');
				}

				//html メソッドで値をセットする
				label.html(html == "" ? '(none)' : html);
			}

			//input の場合
			else{
				label.text(value == "" ? '(none)' : value);
			}

			hideEditor();
		}

		//表示用テキストに初期値をセット
		saveEdit();

		//編集モードへの切替イベント
		label.bind('click',function(){
			showEditor();
		});

		//取消,確定のショートカットキーの割当
		editor.bind('keypress',function(evt){
			if(evt.keyCode == 27){ //ESC
				cancelEdit();
			}
			else
			if(!isTextarea && evt.keyCode == 13){ //ENTER
				saveEdit();
			}
		});

		//取消,確定ボタンクリック時処理
		cmdSet.bind('click',function(evt){
			var target = $(evt.target);
			if(target.hasClass('esc')){
				cancelEdit();
			}
			else
			if(target.hasClass('save')){
				saveEdit();
			}
		});

		//起動時に編集モードにする
		if(c.openEdit){
			showEditor();
		}

	});
}
})(jQuery);
