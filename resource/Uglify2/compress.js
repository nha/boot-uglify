/* http://lisperator.net/uglifyjs/compress */
function compress(code, mangle) {
    var ast = UglifyJS.parse(code);
    ast.figure_out_scope();

    var compressor = UglifyJS.Compressor({});
    ast = ast.transform(compressor);

    if (mangle) {
	ast.figure_out_scope();
	ast.compute_char_frequency();
	ast.mangle_names();
    }

    return ast.print_to_string();
}
