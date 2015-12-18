/* http://lisperator.net/uglifyjs/compress */
//print('loading compress');
function compress(code, options) {
    //print('calling compress');
    var ast, compressor;

    options = options || {};

    ast = UglifyJS.parse(code);
    ast.figure_out_scope();

    compressor = UglifyJS.Compressor(options);
    ast = ast.transform(compressor);

    if (options.mangle) {
	ast.figure_out_scope();
	ast.compute_char_frequency();
	ast.mangle_names();
    }

    return ast.print_to_string();
}

//print(compress("var b = function ok() { print('okkk'); return 123;}"));
